package org.acme.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.ConfiguracaoSistema;
import org.acme.entity.HistoricoCotacao;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class LimpezaHistoricoService {

    private static final Logger LOG = Logger.getLogger(LimpezaHistoricoService.class);
    private static final String CHAVE_DIAS_MANUTENCAO = "DIAS_MANUTENCAO_HISTORICO";
    private static final String CHAVE_SCHEDULER_ATIVO = "SCHEDULER_LIMPEZA_ATIVO";

    private final Map<String, Object> status = new ConcurrentHashMap<>();
    private final AtomicLong totalRegistrosApagados = new AtomicLong(0);

    public LimpezaHistoricoService() {
        status.put("ativo", true);
        status.put("ultimaExecucao", "Nunca");
        status.put("proximaExecucao", "Em breve");
        status.put("totalRegistrosApagados", 0L);
    }

    /**
     * Executa a limpeza a cada 1 hora (3600 segundos)
     * Delay inicial de 5 minutos para garantir inicialização completa
     */
    @Scheduled(every = "1h", delayed = "5m")
    @Transactional
    public void executarLimpezaAutomatica() {
        try {
            if (!isSchedulerAtivo()) {
                LOG.debug("Scheduler de limpeza está pausado. Execução ignorada.");
                return;
            }
            limparHistoricosAntigos();
        } catch (Exception e) {
            LOG.error("Erro no scheduler de limpeza: " + e.getMessage(), e);
        }
    }

    /**
     * Limpa históricos mais antigos que o parâmetro configurado
     */
    @Transactional
    public Map<String, Object> limparHistoricosAntigos() {
        try {
            int dias = getDiasManutencao();
            LocalDate dataLimite = LocalDate.now().minusDays(dias);
            ZoneId zoneId = ZoneId.systemDefault();
            if (zoneId == null) {
                zoneId = ZoneId.of("America/Sao_Paulo");
            }
            LocalDateTime limite = dataLimite.atStartOfDay(zoneId).toLocalDateTime();

            LOG.info("Iniciando limpeza de históricos anteriores a " + dataLimite + " (" + dias + " dias)");
            LOG.debug("Limite: " + limite);

            // Deleta históricos mais antigos que a data limite
            long apagados = HistoricoCotacao.delete("dataHoraConsulta < ?1", limite);

            totalRegistrosApagados.addAndGet(apagados);
            Instant agora = Instant.now();
            status.put("ultimaExecucao", agora);
            status.put("totalRegistrosApagados", totalRegistrosApagados.get());

            LOG.info("Limpeza concluída: " + apagados + " registros apagados");

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("sucesso", true);
            resultado.put("registrosApagados", apagados);
            resultado.put("dataLimite", dataLimite.toString());
            resultado.put("totalAcumulado", totalRegistrosApagados.get());
            return resultado;
        } catch (Exception e) {
            LOG.error("Erro na limpeza de históricos: " + e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("sucesso", false);
            erro.put("erro", e.getMessage());
            return erro;
        }
    }

    public int getDiasManutencao() {
        ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_DIAS_MANUTENCAO).firstResult();
        if (config != null) {
            try {
                return Integer.parseInt(config.valor);
            } catch (NumberFormatException e) {
                LOG.warn("Valor inválido para dias de manutenção, usando padrão 6");
            }
        }
        return 6; // padrão
    }

    @Transactional
    public void setDiasManutencao(int dias) {
        ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_DIAS_MANUTENCAO).firstResult();
        if (config == null) {
            config = new ConfiguracaoSistema(CHAVE_DIAS_MANUTENCAO, String.valueOf(dias));
            config.persist();
        } else {
            config.valor = String.valueOf(dias);
            config.persist();
        }
        LOG.info("Dias de manutenção configurado para: " + dias);
    }

    public boolean isSchedulerAtivo() {
        ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_SCHEDULER_ATIVO).firstResult();
        if (config == null) {
            return true; // padrão é ativo
        }
        return Boolean.parseBoolean(config.valor);
    }

    @Transactional
    public void setSchedulerAtivo(boolean ativo) {
        ConfiguracaoSistema config = ConfiguracaoSistema.find("chave", CHAVE_SCHEDULER_ATIVO).firstResult();
        if (config == null) {
            config = new ConfiguracaoSistema(CHAVE_SCHEDULER_ATIVO, String.valueOf(ativo));
            config.persist();
        } else {
            config.valor = String.valueOf(ativo);
            config.persist();
        }
        status.put("ativo", ativo);
        LOG.info("Scheduler de limpeza " + (ativo ? "ativado" : "desativado"));
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("ativo", isSchedulerAtivo());
        statusMap.put("ultimaExecucao", getUltimaExecucaoParaStatus());
        statusMap.put("proximaExecucao", calcularProximaExecucao());
        statusMap.put("diasManutencao", getDiasManutencao());
        statusMap.put("totalRegistrosApagados", totalRegistrosApagados.get());
        return statusMap;
    }

    private Object getUltimaExecucaoParaStatus() {
        Object valor = status.get("ultimaExecucao");
        if (valor instanceof Instant) {
            return valor;
        }
        return null; // Nunca executou
    }

    private String calcularProximaExecucao() {
        if (!isSchedulerAtivo()) {
            return "Pausado";
        }
        Object valor = status.get("ultimaExecucao");
        if (!(valor instanceof Instant)) {
            return "Em breve";
        }
        Instant ultima = (Instant) valor;
        Instant proxima = ultima.plusSeconds(3600); // 1 hora
        return proxima.toString();
    }
}
