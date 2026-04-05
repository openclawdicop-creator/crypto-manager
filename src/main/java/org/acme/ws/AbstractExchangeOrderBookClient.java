package org.acme.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.acme.entity.CategoriaExchange;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.acme.entity.Proxy;
import org.acme.repository.ProxyRepository;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AbstractExchangeOrderBookClient {

    private static final int DEFAULT_DEPTH_LIMIT = 10;
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ProxyRepository proxyRepository;

    public final ResultadoCotacao consultarPreco(ParametrizacaoConsultaPreco parametrizacao) {
        validarParametrizacao(parametrizacao);

        String symbol = normalizarSimbolo(parametrizacao.identificadorNegociacao.trim());
        int depthLimit = obterProfundidadeLivroOfertas(parametrizacao);
        boolean usarProxy = parametrizacao.exchange != null && Boolean.TRUE.equals(parametrizacao.exchange.usarProxy);
        boolean logHabilitado = parametrizacao.logHabilitado
                || (parametrizacao.exchange != null && parametrizacao.exchange.logHabilitado);

        if (logHabilitado) {
            System.out.println("Iniciando consulta " + getNomeExchange() + " para symbol: " + symbol
                    + " " + getDetalhesConsultaLog(parametrizacao, depthLimit)
                    + (usarProxy ? " (com Proxy)" : " (direto)"));
        }

        String urlConsulta = construirUrlConsulta(parametrizacao, symbol, depthLimit);
        String responseBody = usarProxy
                ? executarConsultaComFallback(urlConsulta, logHabilitado)
                : executarConsultaDireta(urlConsulta, logHabilitado);

        if (logHabilitado) {
            System.out.println("Retorno API " + getNomeExchange() + ": "
                    + (responseBody != null && responseBody.length() > 100
                    ? responseBody.substring(0, 100) + "..."
                    : responseBody));
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode livroOfertas = extrairLivroOfertas(root);
            BigDecimal valorFinanceiroTotal = BigDecimal.valueOf(parametrizacao.quantidadePagamento);

            double cotacaoCompra = calcularPrecoMedioPonderado(
                    obterOfertasCompra(livroOfertas),
                    valorFinanceiroTotal,
                    "compra");
            double cotacaoVenda = calcularPrecoMedioPonderado(
                    obterOfertasVenda(livroOfertas),
                    valorFinanceiroTotal,
                    "venda");

            return new ResultadoCotacao(cotacaoCompra, cotacaoVenda);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao processar o retorno da " + getNomeExchange() + ".", e);
        }
    }

    protected abstract String getNomeExchange();

    protected abstract String construirUrlConsulta(ParametrizacaoConsultaPreco parametrizacao, String symbol, int depthLimit);

    protected abstract JsonNode obterOfertasCompra(JsonNode livroOfertas);

    protected abstract JsonNode obterOfertasVenda(JsonNode livroOfertas);

    protected JsonNode extrairLivroOfertas(JsonNode root) {
        return root;
    }

    protected String getDetalhesConsultaLog(ParametrizacaoConsultaPreco parametrizacao, int depthLimit) {
        return "[depthLimit=" + depthLimit + "]";
    }

    protected String normalizarSimbolo(String simbolo) {
        return simbolo.toUpperCase(Locale.ROOT);
    }

    protected CategoriaExchange obterCategoriaExchange(ParametrizacaoConsultaPreco parametrizacao) {
        if (parametrizacao == null || parametrizacao.exchange == null || parametrizacao.exchange.categoria == null) {
            return CategoriaExchange.SPOT;
        }
        return parametrizacao.exchange.categoria;
    }

    protected String obterBaseUrl(ParametrizacaoConsultaPreco parametrizacao) {
        return parametrizacao.exchange.urlApi.trim().replaceAll("/+$", "");
    }

    protected int obterProfundidadeLivroOfertas(ParametrizacaoConsultaPreco parametrizacao) {
        Integer profundidadeLivroOfertas = parametrizacao.exchange != null
                ? parametrizacao.exchange.profundidadeLivroOfertas
                : null;

        return profundidadeLivroOfertas != null && profundidadeLivroOfertas > 0
                ? profundidadeLivroOfertas
                : DEFAULT_DEPTH_LIMIT;
    }

    protected double calcularPrecoMedioPonderado(JsonNode ofertas, BigDecimal valorFinanceiroDesejado, String lado) {
        if (ofertas == null || !ofertas.isArray() || ofertas.isEmpty()) {
            throw new IllegalStateException("Livro de ofertas vazio para operacao de " + lado + ".");
        }

        BigDecimal valorFinanceiroRestante = valorFinanceiroDesejado;
        BigDecimal valorFinanceiroExecutado = BigDecimal.ZERO;
        BigDecimal quantidadeBaseExecutada = BigDecimal.ZERO;

        for (JsonNode oferta : ofertas) {
            if (!oferta.isArray() || oferta.size() < 2) {
                continue;
            }

            BigDecimal preco = new BigDecimal(oferta.get(0).asText());
            BigDecimal quantidadeDisponivel = new BigDecimal(oferta.get(1).asText());
            BigDecimal valorFinanceiroDisponivel = preco.multiply(quantidadeDisponivel, MATH_CONTEXT);

            if (valorFinanceiroRestante.compareTo(valorFinanceiroDisponivel) >= 0) {
                valorFinanceiroExecutado = valorFinanceiroExecutado.add(valorFinanceiroDisponivel, MATH_CONTEXT);
                quantidadeBaseExecutada = quantidadeBaseExecutada.add(quantidadeDisponivel, MATH_CONTEXT);
                valorFinanceiroRestante = valorFinanceiroRestante.subtract(valorFinanceiroDisponivel, MATH_CONTEXT);
            } else {
                BigDecimal quantidadeParcial = valorFinanceiroRestante.divide(preco, MATH_CONTEXT);
                valorFinanceiroExecutado = valorFinanceiroExecutado.add(valorFinanceiroRestante, MATH_CONTEXT);
                quantidadeBaseExecutada = quantidadeBaseExecutada.add(quantidadeParcial, MATH_CONTEXT);
                valorFinanceiroRestante = BigDecimal.ZERO;
            }

            if (valorFinanceiroRestante.signum() <= 0) {
                break;
            }
        }

        if (quantidadeBaseExecutada.signum() == 0 || valorFinanceiroRestante.signum() > 0) {
            throw new IllegalStateException("Profundidade insuficiente para calcular a cotacao de " + lado
                    + " com o valor financeiro informado.");
        }

        return valorFinanceiroExecutado.divide(quantidadeBaseExecutada, MATH_CONTEXT).doubleValue();
    }

    private String executarConsultaComFallback(String urlConsulta, boolean logHabilitado) {
        try {
            return executarComProxyRotation(urlConsulta, logHabilitado);
        } catch (IllegalStateException e) {
            if (logHabilitado) {
                System.err.println("Todos os proxies falharam. Tentando consulta direta como fallback: " + e.getMessage());
            }
            return executarConsultaDireta(urlConsulta, logHabilitado);
        }
    }

    private String executarConsultaDireta(String urlConsulta, boolean logHabilitado) {
        try {
            if (logHabilitado) {
                System.out.println("Executando consulta direta: " + urlConsulta);
            }
            return Request.Get(urlConsulta)
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute()
                    .returnContent()
                    .asString();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar a " + getNomeExchange() + " (direto): " + e.getMessage(), e);
        }
    }

    private String executarComProxyRotation(String urlConsulta, boolean logHabilitado) {
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
                System.out.println("Tentando proxy: " + (proxy.nome != null ? proxy.nome : proxy.url)
                        + " (" + proxyHostStr + ":" + porta + ")");
            }

            try {
                Executor executor = Executor.newInstance();
                boolean temAuth = proxy.usuario != null && !proxy.usuario.isBlank();

                if (temAuth) {
                    if (logHabilitado) {
                        System.out.println("Configurando autenticacao para o proxy: " + proxy.usuario);
                    }
                    executor.auth(proxyHost, proxy.usuario, proxy.senha);
                }

                return executor.execute(
                                Request.Get(urlConsulta)
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
                    if (logHabilitado) {
                        System.out.println("Erro recuperavel detectado. Continuando rotacao...");
                    }
                    continue;
                }
            }
        }

        throw new IllegalStateException("API " + getNomeExchange() + " inacessivel apos esgotar todos os proxies disponiveis ("
                + listaParaTentativa.size() + "). Erros: " + String.join(" | ", erros));
    }

    private void validarParametrizacao(ParametrizacaoConsultaPreco parametrizacao) {
        if (parametrizacao == null) {
            throw new IllegalArgumentException("Parametrizacao obrigatoria para consultar preco.");
        }
        if (parametrizacao.exchange == null) {
            throw new IllegalArgumentException("Exchange obrigatoria para consultar a " + getNomeExchange() + ".");
        }
        if (parametrizacao.exchange.urlApi == null || parametrizacao.exchange.urlApi.isBlank()) {
            throw new IllegalArgumentException("URL da API da exchange obrigatoria para consultar a " + getNomeExchange() + ".");
        }
        if (parametrizacao.identificadorNegociacao == null || parametrizacao.identificadorNegociacao.isBlank()) {
            throw new IllegalArgumentException("Identificador de negociacao obrigatorio para consultar a " + getNomeExchange() + ".");
        }
        if (parametrizacao.quantidadePagamento == null || parametrizacao.quantidadePagamento <= 0) {
            throw new IllegalArgumentException("Quantidade de pagamento deve ser maior que zero.");
        }
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
}
