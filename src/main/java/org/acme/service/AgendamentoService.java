package org.acme.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Agendamento;
import org.acme.entity.ParametrizacaoConsultaPreco;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgendamentoService {

    private static final Logger LOG = Logger.getLogger(AgendamentoService.class);

    @Inject
    ParametrizacaoConsultaPrecoService parametrizacaoService;

    @Inject
    CotacaoService cotacaoService;

    // Map para controle de última execução por agendamento (não para agendamento em si)
    private final Map<Long, Instant> ultimaExecucao = new ConcurrentHashMap<>();

    /**
     * Polling a cada segundo para verificar agendamentos ativos e executar os que estão devido.
     * Esta abordagem evita o problema de classloader do Quarkus Dev Mode causado por lambdas.
     */
    @Scheduled(every = "1s")
    void verificarAgendamentos() {
        try {
            List<Agendamento> agendamentosAtivos = Agendamento.list("ativo", true);
            Instant agora = Instant.now();
            
            for (Agendamento agendamento : agendamentosAtivos) {
                Instant ultimaExec = ultimaExecucao.get(agendamento.id);
                long frequenciaSegundos = agendamento.frequenciaSegundos != null ? agendamento.frequenciaSegundos : 60;
                
                // Verifica se já passou o tempo necessário desde a última execução
                if (ultimaExec == null || agora.isAfter(ultimaExec.plusSeconds(frequenciaSegundos))) {
                    LOG.debug("Executando agendamento: " + agendamento.nome + " (ID: " + agendamento.id + ")");
                    // Marca a última execução ANTES de chamar a API para evitar que o tick do próximo segundo dispare novamente
                    ultimaExecucao.put(agendamento.id, agora);
                    executarRotina(agendamento.id);
                }
            }
        } catch (Exception e) {
            LOG.error("Erro ao verificar agendamentos: " + e.getMessage());
        }
    }

    public List<Agendamento> listarTodos() {
        return Agendamento.listAll();
    }

    @Transactional
    public Agendamento criar(Agendamento dados) {
        dados.persist();
        LOG.info("Agendamento criado: " + dados.nome + " a cada " + dados.frequenciaSegundos + "s");
        // Não precisa agendar manualmente - o polling @Scheduled vai detectar
        return dados;
    }

    @Transactional
    public Agendamento atualizar(Long id, Agendamento dados) {
        Agendamento agendamento = Agendamento.findById(id);
        if (agendamento == null) {
            throw new RuntimeException("Agendamento não encontrado");
        }
        agendamento.nome = dados.nome;
        agendamento.frequenciaSegundos = dados.frequenciaSegundos;
        agendamento.ativo = dados.ativo;
        agendamento.persist();

        // Atualiza o controle de última execução para forçar reavaliação imediata
        if (dados.ativo) {
            ultimaExecucao.remove(id);
            LOG.info("Agendamento atualizado e reavaliado: " + agendamento.nome);
        } else {
            ultimaExecucao.remove(id);
            LOG.info("Agendamento desativado: " + agendamento.nome);
        }
        return agendamento;
    }

    void executarRotina(Long agendamentoId) {
        try {
            LOG.info("Executando rotina de agendamento ID: " + agendamentoId);
            List<ParametrizacaoConsultaPreco> ativas = parametrizacaoService.listarPorAtiva(true);
            for (ParametrizacaoConsultaPreco p : ativas) {
                try {
                    processarConsultaSeguro(p);
                } catch (Exception e) {
                    LOG.error("Erro ao processar consulta ID " + p.id + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("Erro na execução da rotina: " + e.getMessage());
        }
    }

    @Transactional
    public void processarConsultaSeguro(ParametrizacaoConsultaPreco p) {
        LOG.info("Processando consulta para: " + descreverParametrizacao(p));
        cotacaoService.processarConsulta(p);
    }

    private String descreverParametrizacao(ParametrizacaoConsultaPreco parametrizacao) {
        if (parametrizacao.identificadorNegociacao != null && !parametrizacao.identificadorNegociacao.isBlank()) {
            return parametrizacao.identificadorNegociacao;
        }

        String ativoDesejado = parametrizacao.ativoDesejado != null ? parametrizacao.ativoDesejado.simbolo : "?";
        String ativoPagamento = parametrizacao.ativoPagamento != null ? parametrizacao.ativoPagamento.simbolo : "?";
        String rede = parametrizacao.rede != null ? parametrizacao.rede.nome : "?";
        return ativoDesejado + "/" + ativoPagamento + " @ " + rede;
    }
}
