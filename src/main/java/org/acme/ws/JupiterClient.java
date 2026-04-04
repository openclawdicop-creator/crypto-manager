package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.entity.AtivoFinanceiro;
import org.acme.entity.AtivoFinanceiroRede;
import org.acme.entity.Exchange;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.entity.Proxy;
import org.acme.repository.AtivoFinanceiroRedeRepository;
import org.acme.repository.ProxyRepository;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class JupiterClient {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ProxyRepository proxyRepository;

    @Inject
    AtivoFinanceiroRedeRepository ativoFinanceiroRedeRepository;

    public ResultadoCotacao consultarPreco(ParametrizacaoConsultaPreco parametrizacao) {
        validarParametrizacao(parametrizacao);

        Exchange exchange = parametrizacao.exchange;
        boolean usarProxy = Boolean.TRUE.equals(exchange.usarProxy);
        boolean logHabilitado = parametrizacao.logHabilitado || exchange.logHabilitado;

        TokenInfo tokenPagamento = resolverToken(parametrizacao.ativoPagamento, parametrizacao.rede.id);
        TokenInfo tokenDesejado = resolverToken(parametrizacao.ativoDesejado, parametrizacao.rede.id);

        BigDecimal quantidadePagamento = BigDecimal.valueOf(parametrizacao.quantidadePagamento);
        String quantidadePagamentoMenorUnidade = toMinorUnits(quantidadePagamento, tokenPagamento.quantidadeCasasDecimais());

        if (logHabilitado) {
            System.out.println("Iniciando consulta Jupiter para parametrizacao #" + parametrizacao.id
                    + " [" + tokenPagamento.simbolo() + " -> " + tokenDesejado.simbolo()
                    + ", rede=" + parametrizacao.rede.nome
                    + ", amount=" + quantidadePagamentoMenorUnidade
                    + ", baseUrl=" + sanitizeBaseUrl(exchange.urlApi)
                    + (usarProxy ? ", com Proxy" : ", direto") + "]");
        }

        JsonNode respostaCompra = consultarOrder(
                exchange,
                tokenPagamento.identificador(),
                tokenDesejado.identificador(),
                quantidadePagamentoMenorUnidade,
                usarProxy,
                logHabilitado,
                "compra");

        String quantidadeCompradaMenorUnidade = obterCampoObrigatorio(respostaCompra, "outAmount", "compra");
        BigDecimal quantidadeComprada = fromMinorUnits(
                quantidadeCompradaMenorUnidade,
                tokenDesejado.quantidadeCasasDecimais());

        if (quantidadeComprada.signum() <= 0) {
            throw new IllegalStateException("Jupiter retornou quantidade comprada zerada para a consulta de compra.");
        }

        BigDecimal precoCompra = quantidadePagamento.divide(quantidadeComprada, MATH_CONTEXT);

        JsonNode respostaVenda = consultarOrder(
                exchange,
                tokenDesejado.identificador(),
                tokenPagamento.identificador(),
                quantidadeCompradaMenorUnidade,
                usarProxy,
                logHabilitado,
                "venda");

        String quantidadeRecebidaVendaMenorUnidade = obterCampoObrigatorio(respostaVenda, "outAmount", "venda");
        BigDecimal quantidadeRecebidaVenda = fromMinorUnits(
                quantidadeRecebidaVendaMenorUnidade,
                tokenPagamento.quantidadeCasasDecimais());

        if (quantidadeRecebidaVenda.signum() <= 0) {
            throw new IllegalStateException("Jupiter retornou quantidade zerada do token de pagamento na consulta de venda.");
        }

        BigDecimal precoVenda = quantidadeRecebidaVenda.divide(quantidadeComprada, MATH_CONTEXT);

        if (logHabilitado) {
            System.out.println("Jupiter respondeu com quantidadeComprada=" + quantidadeComprada.toPlainString()
                    + " " + tokenDesejado.simbolo()
                    + ", quantidadeRecebidaVenda=" + quantidadeRecebidaVenda.toPlainString()
                    + " " + tokenPagamento.simbolo()
                    + ", precoCompra=" + precoCompra.toPlainString()
                    + ", precoVenda=" + precoVenda.toPlainString());
        }

        return new ResultadoCotacao(precoCompra.doubleValue(), precoVenda.doubleValue());
    }

    private JsonNode consultarOrder(Exchange exchange,
                                    String inputMint,
                                    String outputMint,
                                    String amount,
                                    boolean usarProxy,
                                    boolean logHabilitado,
                                    String operacao) {
        String urlConsulta = construirUrl(exchange, inputMint, outputMint, amount);
        String responseBody = usarProxy
                ? executarConsultaComFallback(exchange, urlConsulta, logHabilitado)
                : executarConsultaDireta(exchange, urlConsulta, logHabilitado);

        if (logHabilitado) {
            System.out.println("Retorno API Jupiter (" + operacao + "): "
                    + (responseBody != null && responseBody.length() > 180
                    ? responseBody.substring(0, 180) + "..."
                    : responseBody));
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            validarResposta(root, operacao);
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao processar o retorno da Jupiter na consulta de " + operacao + ".", e);
        }
    }

    private void validarResposta(JsonNode root, String operacao) {
        if (root == null || root.isNull()) {
            throw new IllegalStateException("Resposta vazia da Jupiter na consulta de " + operacao + ".");
        }

        String erro = textoOpcional(root, "errorMessage");
        if (erro == null || erro.isBlank()) {
            erro = textoOpcional(root, "error");
        }

        if (erro != null && !erro.isBlank()) {
            throw new IllegalStateException("Erro retornado pela Jupiter na consulta de " + operacao + ": " + erro);
        }

        if (textoOpcional(root, "outAmount") == null) {
            throw new IllegalStateException("Resposta da Jupiter sem outAmount na consulta de " + operacao + ".");
        }
    }

    private TokenInfo resolverToken(AtivoFinanceiro ativo, Long redeId) {
        if (ativo == null || ativo.id == null) {
            throw new IllegalArgumentException("Ativo financeiro obrigatorio para consultar cotacao na Jupiter.");
        }

        AtivoFinanceiroRede ativoFinanceiroRede = ativoFinanceiroRedeRepository
                .findByAtivoAndRede(ativo.id, redeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nao existe AtivoFinanceiroRede cadastrado para o ativo "
                                + ativo.simbolo + " na rede selecionada."));

        if (ativoFinanceiroRede.identificador == null || ativoFinanceiroRede.identificador.isBlank()) {
            throw new IllegalArgumentException("O identificador do token na rede e obrigatorio para o ativo "
                    + ativo.simbolo + ".");
        }

        if (ativoFinanceiroRede.quantidadeCasasDecimais == null || ativoFinanceiroRede.quantidadeCasasDecimais < 0) {
            throw new IllegalArgumentException("A quantidade de casas decimais do token e obrigatoria para o ativo "
                    + ativo.simbolo + ".");
        }

        return new TokenInfo(
                ativo.simbolo,
                ativoFinanceiroRede.identificador.trim(),
                ativoFinanceiroRede.quantidadeCasasDecimais);
    }

    private String construirUrl(Exchange exchange, String inputMint, String outputMint, String amount) {
        return sanitizeBaseUrl(exchange.urlApi)
                + "/swap/v2/order?inputMint=" + encode(inputMint)
                + "&outputMint=" + encode(outputMint)
                + "&amount=" + encode(amount)
                + "&swapMode=ExactIn";
    }

    private String executarConsultaComFallback(Exchange exchange, String urlConsulta, boolean logHabilitado) {
        try {
            return executarComProxyRotation(exchange, urlConsulta, logHabilitado);
        } catch (IllegalStateException e) {
            if (logHabilitado) {
                System.err.println("Todos os proxies falharam. Tentando consulta direta como fallback: " + e.getMessage());
            }
            return executarConsultaDireta(exchange, urlConsulta, logHabilitado);
        }
    }

    private String executarConsultaDireta(Exchange exchange, String urlConsulta, boolean logHabilitado) {
        try {
            if (logHabilitado) {
                System.out.println("Executando consulta direta Jupiter: " + urlConsulta);
            }
            return Request.Get(urlConsulta)
                    .addHeader("Accept", "application/json")
                    .addHeader("x-api-key", exchange.tokenApi.trim())
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute()
                    .returnContent()
                    .asString();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar a Jupiter (direto): " + e.getMessage(), e);
        }
    }

    private String executarComProxyRotation(Exchange exchange, String urlConsulta, boolean logHabilitado) {
        List<Proxy> proxies = proxyRepository.listAll();
        if (proxies == null || proxies.isEmpty()) {
            throw new IllegalStateException("Uso de proxy habilitado, mas nenhum proxy cadastrado no banco de dados.");
        }

        List<Proxy> listaParaTentativa = new ArrayList<>(proxies);
        Collections.shuffle(listaParaTentativa);

        List<String> erros = new ArrayList<>();
        for (Proxy proxy : listaParaTentativa) {
            int porta = proxy.porta != null ? proxy.porta : 80;
            String proxyHostStr = limparHost(proxy.url);
            HttpHost proxyHost = new HttpHost(proxyHostStr, porta);

            if (logHabilitado) {
                System.out.println("Tentando proxy Jupiter: " + (proxy.nome != null ? proxy.nome : proxy.url)
                        + " (" + proxyHostStr + ":" + porta + ")");
            }

            try {
                Executor executor = Executor.newInstance();
                boolean temAuth = proxy.usuario != null && !proxy.usuario.isBlank();

                if (temAuth) {
                    executor.auth(proxyHost, proxy.usuario, proxy.senha);
                }

                return executor.execute(
                                Request.Get(urlConsulta)
                                        .addHeader("Accept", "application/json")
                                        .addHeader("x-api-key", exchange.tokenApi.trim())
                                        .viaProxy(proxyHost)
                                        .connectTimeout(5000)
                                        .socketTimeout(5000))
                        .returnContent()
                        .asString();
            } catch (Exception e) {
                String erroMsg = "Falha no proxy " + (proxy.nome != null ? proxy.nome : proxy.url) + ": " + e.getMessage();
                erros.add(erroMsg);

                if (logHabilitado) {
                    System.err.println(erroMsg);
                }

                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("407") || msg.contains("451") || msg.contains("402") || e instanceof IOException) {
                    continue;
                }
            }
        }

        throw new IllegalStateException("API Jupiter inacessivel apos esgotar todos os proxies disponiveis ("
                + listaParaTentativa.size() + "). Erros: " + String.join(" | ", erros));
    }

    private void validarParametrizacao(ParametrizacaoConsultaPreco parametrizacao) {
        if (parametrizacao == null) {
            throw new IllegalArgumentException("Parametrizacao obrigatoria para consultar a Jupiter.");
        }
        if (parametrizacao.exchange == null) {
            throw new IllegalArgumentException("Exchange obrigatoria para consultar a Jupiter.");
        }
        if (parametrizacao.rede == null || parametrizacao.rede.id == null) {
            throw new IllegalArgumentException("Rede obrigatoria para consultar a Jupiter.");
        }
        if (parametrizacao.ativoDesejado == null || parametrizacao.ativoDesejado.id == null) {
            throw new IllegalArgumentException("Ativo desejado obrigatorio para consultar a Jupiter.");
        }
        if (parametrizacao.ativoPagamento == null || parametrizacao.ativoPagamento.id == null) {
            throw new IllegalArgumentException("Ativo de pagamento obrigatorio para consultar a Jupiter.");
        }
        if (parametrizacao.exchange.urlApi == null || parametrizacao.exchange.urlApi.isBlank()) {
            throw new IllegalArgumentException("URL da API da exchange obrigatoria para consultar a Jupiter.");
        }
        if (parametrizacao.quantidadePagamento == null || parametrizacao.quantidadePagamento <= 0) {
            throw new IllegalArgumentException("Quantidade de pagamento deve ser maior que zero.");
        }
    }

    private String obterCampoObrigatorio(JsonNode root, String campo, String operacao) {
        String valor = textoOpcional(root, campo);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Resposta da Jupiter sem o campo " + campo + " na consulta de " + operacao + ".");
        }
        return valor;
    }

    private String textoOpcional(JsonNode root, String campo) {
        JsonNode node = root.get(campo);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private String toMinorUnits(BigDecimal amount, int decimals) {
        try {
            return amount.movePointRight(decimals)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .toPlainString();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("A quantidade informada possui mais casas decimais do que o token suporta.", e);
        }
    }

    private BigDecimal fromMinorUnits(String amount, int decimals) {
        return new BigDecimal(amount).movePointLeft(decimals);
    }

    private String sanitizeBaseUrl(String url) {
        return url.trim().replaceAll("/+$", "");
    }

    private String encode(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private String limparHost(String url) {
        if (url == null) {
            return null;
        }
        String host = url.replaceFirst("^[a-zA-Z]+://", "");
        host = host.split("/")[0];
        host = host.split(":")[0];
        return host;
    }

    private record TokenInfo(String simbolo, String identificador, int quantidadeCasasDecimais) {
    }
}
