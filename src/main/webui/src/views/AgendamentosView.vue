<template>
  <div class="agendamento-container">
    <!-- Seção de Limpeza de Históricos -->
    <div class="glass-card limpeza-section">
      <div class="section-header">
        <h3>🗑️ Limpeza Automática de Históricos</h3>
      </div>
      
      <div class="limpeza-info">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Status</span>
            <span :class="['status-badge', limpezaStatus.ativo ? 'active' : 'inactive', 'status-value']">
              {{ getStatusLabel(limpezaStatus.ativo) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">Dias de retenção</span>
            <div class="dias-edit" v-if="editandoDias">
              <input type="number" v-model.number="diasEdicao" min="1" max="365" class="dias-input" />
              <button @click="salvarDias" class="icon-btn save-btn" title="Salvar">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
              </button>
              <button @click="cancelarEdicao" class="icon-btn cancel-btn" title="Cancelar">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div v-else class="dias-display">
              <span class="info-value">{{ limpezaStatus.diasManutencao }} dias</span>
              <button @click="iniciarEdicaoDias" class="icon-btn edit-btn-small" title="Editar dias">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">Última execução</span>
            <span class="info-value">{{ formatarData(limpezaStatus.ultimaExecucao) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Próxima execução</span>
            <span class="info-value">{{ limpezaStatus.proximaExecucao }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Total apagado</span>
            <span class="info-value">{{ limpezaStatus.totalRegistrosApagados }} registros</span>
          </div>
        </div>
      </div>

      <div class="limpeza-actions">
        <button @click="executarLimpeza" :disabled="executandoLimpeza" class="action-btn primary-btn">
          <svg v-if="!executandoLimpeza" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          <span v-if="executandoLimpeza" class="spinner-small"></span>
          {{ executandoLimpeza ? 'Executando...' : 'Executar Limpeza Agora' }}
        </button>
        <button v-if="limpezaStatus.ativo" @click="pausarScheduler" :disabled="operandoScheduler" class="action-btn warning-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="6" width="12" height="12"/></svg>
          Pausar Agendamento
        </button>
        <button v-else @click="iniciarScheduler" :disabled="operandoScheduler" class="action-btn success-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          Iniciar Agendamento
        </button>
      </div>
    </div>

    <!-- Alert Notifications -->
    <div v-if="alertMessage" :class="['alert', alertType, 'floating-alert']">
      <div class="alert-content">
        <svg v-if="alertType === 'success'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else-if="alertType === 'error'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <span>{{ alertMessage }}</span>
      </div>
      <button @click="alertMessage = ''" class="alert-close">&times;</button>
    </div>

    <!-- Seção de Agendamentos de Consulta -->
    <div class="glass-card consultas-section">
      <div class="section-header">
        <h3>📅 Agendamentos de Consulta</h3>
      </div>

      <div v-if="loading" class="loading-state">
        <span class="spinner"></span> Carregando agendamentos...
      </div>
      
      <div class="consultas-list" v-else>
        <div v-if="agendamentos.length === 0" class="empty-state">
          Nenhum agendamento encontrado no banco de dados.
        </div>
        
        <div v-for="a in agendamentos" :key="a.id" class="consulta-card-item">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Status</span>
              <span :class="['status-badge', a.ativo ? 'active' : 'inactive', 'status-value']">
                {{ getStatusLabel(a.ativo) }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">Nome da Rotina</span>
              <span class="info-value">{{ a.nome }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Frequência</span>
              <div class="freq-edit" v-if="editingId === a.id">
                <input type="number" v-model.number="editFreq" min="5" class="dias-input" />
                <button @click="saveFreq(a)" class="icon-btn save-btn" title="Salvar">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </button>
                <button @click="editingId = null" class="icon-btn cancel-btn" title="Cancelar">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
                </button>
              </div>
              <div v-else class="freq-display">
                <span class="info-value">{{ a.frequenciaSegundos }}s</span>
                <button @click="startEdit(a)" class="icon-btn edit-btn-small" title="Editar freqüência">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                </button>
              </div>
            </div>
          </div>
          
          <div class="limpeza-actions" style="margin-top: 1.5rem;">
            <button
              v-if="!a.ativo"
              @click="toggleAtivo(a, true)"
              class="action-btn success-btn"
              :disabled="togglingId === a.id"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
              Iniciar Agendamento
            </button>
            <button
              v-else
              @click="toggleAtivo(a, false)"
              class="action-btn warning-btn"
              :disabled="togglingId === a.id"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="6" width="12" height="12"/></svg>
              Parar Agendamento
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../utils/api.js'

const agendamentos = ref([])
const loading = ref(true)
const togglingId = ref(null)
const editingId = ref(null)
const editFreq = ref(60)

// Limpeza de históricos
const limpezaStatus = ref({
  ativo: true,
  diasManutencao: 6,
  ultimaExecucao: null,
  proximaExecucao: '-',
  totalRegistrosApagados: 0
})
const executandoLimpeza = ref(false)
const operandoScheduler = ref(false)
const editandoDias = ref(false)
const diasEdicao = ref(6)

const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => (alertMessage.value = ''), 5000)
}

const getStatusLabel = (ativo) => (ativo ? 'Rodando' : 'Parado')

const formatarData = (data) => {
  if (!data) return 'Nunca'
  try {
    const d = new Date(data)
    return d.toLocaleString('pt-BR')
  } catch {
    return data
  }
}

const carregarStatusLimpeza = async () => {
  try {
    const res = await apiFetch('/api/agendamentos/limpeza/status')
    if (res && res.data) {
      limpezaStatus.value = res.data
    }
  } catch (error) {
    console.error('Erro ao carregar status de limpeza:', error)
  }
}

const executarLimpeza = async () => {
  executandoLimpeza.value = true
  try {
    const res = await apiFetch('/api/agendamentos/limpeza/executar', { method: 'POST' })
    if (res && res.error) throw new Error(res.message)
    
    const registros = res.data?.registrosApagados || 0
    showAlert(`Limpeza executada! ${registros} registro(s) apagado(s).`, 'success')
    await carregarStatusLimpeza()
  } catch (error) {
    showAlert(error.message || 'Erro ao executar limpeza', 'error')
  } finally {
    executandoLimpeza.value = false
  }
}

const pausarScheduler = async () => {
  operandoScheduler.value = true
  try {
    const res = await apiFetch('/api/agendamentos/limpeza/pausar', { method: 'POST' })
    if (res && res.error) throw new Error(res.message)
    
    showAlert('Agendamento de limpeza pausado', 'success')
    await carregarStatusLimpeza()
  } catch (error) {
    showAlert(error.message || 'Erro ao pausar scheduler', 'error')
  } finally {
    operandoScheduler.value = false
  }
}

const iniciarScheduler = async () => {
  operandoScheduler.value = true
  try {
    const res = await apiFetch('/api/agendamentos/limpeza/iniciar', { method: 'POST' })
    if (res && res.error) throw new Error(res.message)
    
    showAlert('Agendamento de limpeza ativado', 'success')
    await carregarStatusLimpeza()
  } catch (error) {
    showAlert(error.message || 'Erro ao iniciar scheduler', 'error')
  } finally {
    operandoScheduler.value = false
  }
}

const iniciarEdicaoDias = () => {
  diasEdicao.value = limpezaStatus.value.diasManutencao
  editandoDias.value = true
}

const cancelarEdicao = () => {
  editandoDias.value = false
  diasEdicao.value = limpezaStatus.value.diasManutencao
}

const salvarDias = async () => {
  if (diasEdicao.value < 1 || diasEdicao.value > 365) {
    showAlert('Dias deve estar entre 1 e 365', 'error')
    return
  }
  
  try {
    const res = await apiFetch('/api/parametros/dias-manutencao', {
      method: 'PUT',
      body: JSON.stringify({ valor: diasEdicao.value })
    })
    if (res && res.error) throw new Error(res.message)
    
    showAlert(`Dias de retenção atualizado para ${diasEdicao.value} dias!`, 'success')
    editandoDias.value = false
    await carregarStatusLimpeza()
  } catch (error) {
    showAlert(error.message || 'Erro ao atualizar dias de retenção', 'error')
  }
}

const loadAgendamentos = async () => {
  loading.value = true
  try {
    const res = await apiFetch('/api/agendamentos')
    agendamentos.value = res && res.data ? res.data : []
  } catch (error) {
    showAlert('Erro ao carregar agendamentos', 'error')
  } finally {
    loading.value = false
  }
}

const toggleAtivo = async (agendamento, novoStatus) => {
  togglingId.value = agendamento.id
  const endpoint = novoStatus ? 'iniciar' : 'parar'
  try {
    const res = await apiFetch(`/api/agendamentos/${agendamento.id}/${endpoint}`, { method: 'POST' })
    if (res && res.error) throw new Error(res.message)
    
    showAlert(`Rotina ${novoStatus ? 'iniciada' : 'parada'} com sucesso!`, 'success')
    await loadAgendamentos()
  } catch (error) {
    showAlert(error.message || 'Erro ao alterar status do agendamento', 'error')
  } finally {
    togglingId.value = null
  }
}

const startEdit = (a) => {
  editingId.value = a.id
  editFreq.value = a.frequenciaSegundos
}

const saveFreq = async (a) => {
  try {
    const payload = { ...a, frequenciaSegundos: editFreq.value }
    const res = await apiFetch(`/api/agendamentos/${a.id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
    if (res && res.error) throw new Error(res.message)
    
    showAlert('Frequência atualizada!', 'success')
    editingId.value = null
    await loadAgendamentos()
  } catch (error) {
    showAlert(error.message || 'Erro ao atualizar frequência', 'error')
  }
}

onMounted(() => {
  loadAgendamentos()
  carregarStatusLimpeza()
})
</script>

<style scoped>
.agendamento-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.glass-card {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.03);
  border: 1px solid #f1f5f9;
}

.table-responsive { overflow-x: auto; }

.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  text-align: left;
}

.modern-table th {
  background: #f8fafc;
  color: #64748b;
  font-weight: 600;
  padding: 1rem;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e2e8f0;
}

.modern-table td {
  padding: 1rem;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.modern-table td strong {
  color: #0f172a;
  background: #e2e8f0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}

.modern-table tbody tr { transition: background-color 0.2s; }
.modern-table tbody tr:hover { background-color: #f8fafc; }

.status-badge {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
}
.status-badge.active { background: #dcfce7; color: #15803d; }
.status-badge.inactive { background: #f1f5f9; color: #64748b; }

.actions-col { width: 140px; text-align: center; }

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
}

.start-btn { color: #16a34a; }
.start-btn:hover { background: #f0fdf4; }

.stop-btn { color: #ef4444; }
.stop-btn:hover { background: #fef2f2; }

.icon-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.freq-edit {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.small-input {
  width: 70px;
  padding: 0.25rem 0.5rem;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
}

.freq-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 4px;
}

.edit-btn-small { color: #3b82f6; }
.edit-btn-small:hover { background: #eff6ff; }

.save-btn { color: #16a34a; }
.save-btn:hover { background: #f0fdf4; }

.cancel-btn { color: #ef4444; }
.cancel-btn:hover { background: #fef2f2; }

/* Alert Styles - consistent with other views */
.alert { padding: 1rem; border-radius: 8px; font-weight: 500; }
.alert.success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.alert.error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

.floating-alert {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 2000;
  min-width: 300px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.alert-content { display: flex; align-items: center; gap: 0.75rem; }
.alert-close { background: none; border: none; font-size: 1.25rem; cursor: pointer; color: currentColor; }

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #64748b;
  gap: 1rem;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* Section Styles - Both sections use same layout */
.limpeza-section {
  border-left: 4px solid #3b82f6;
}

.consultas-section {
  border-left: 4px solid #10b981;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.25rem;
  font-weight: 600;
}

.limpeza-info {
  margin-bottom: 1.5rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.75rem;
  color: #64748b;
  text-transform: uppercase;
  font-weight: 600;
}

.info-value {
  font-size: 1rem;
  color: #1e293b;
  font-weight: 500;
}

.status-value {
  align-self: flex-start;
  margin-top: 4px;
}

.limpeza-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-btn {
  background: #3b82f6;
  color: white;
}
.primary-btn:hover:not(:disabled) {
  background: #2563eb;
}

.success-btn {
  background: #16a34a;
  color: white;
}
.success-btn:hover:not(:disabled) {
  background: #15803d;
}

.warning-btn {
  background: #f59e0b;
  color: white;
}
.warning-btn:hover:not(:disabled) {
  background: #d97706;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.consulta-card-item {
  padding: 1.5rem 0;
  border-bottom: 1px solid #f1f5f9;
}

.consulta-card-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.consulta-card-item:first-child {
  padding-top: 0;
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: #64748b;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.dias-edit {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dias-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dias-input {
  width: 80px;
  padding: 0.4rem 0.6rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
}

.dias-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

@media (max-width: 640px) {
  .header-actions { flex-direction: column; align-items: stretch; gap: 0.75rem; width: 100%; }
  .header-actions h2 { font-size: 1.25rem; width: 100%; }
  .header-buttons { width: 100%; flex-direction: column; gap: 0.75rem; }
  .header-buttons > * { width: 100%; }
  .secondary-btn, .header-actions button { width: 100%; justify-content: center; }
  
  .modern-table thead { display: none; }
  .modern-table tr { display: block; margin-bottom: 1rem; border: 1px solid #e2e8f0; border-radius: 8px; }
  .modern-table td { display: flex; justify-content: space-between; text-align: right; padding: 0.75rem 1rem; border-bottom: 1px solid #f1f5f9; }
  .modern-table td::before { content: attr(data-label); font-weight: 600; color: #64748b; }
  .modern-table td:last-child { border-bottom: 0; background: #f8fafc; }
  .actions-col { width: 100%; justify-content: flex-end !important; }
  
  .info-grid { grid-template-columns: 1fr; }
  .limpeza-actions { flex-direction: column; }
  .action-btn { width: 100%; justify-content: center; }
}
</style>
