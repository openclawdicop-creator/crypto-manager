<template>
  <div class="param-container">
    <div class="header-actions">
      <h2>Gerenciamento de Parametrizacoes</h2>
      <button @click="openModal()" class="primary-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
        Nova Parametrizacao
      </button>
    </div>

    <div v-if="alertMessage" :class="['alert', alertType]">
      {{ alertMessage }}
    </div>

    <div v-if="consultaResultado" class="glass-card result-card">
      <div class="result-header">
        <div>
          <h3>Resultado da Ultima Consulta</h3>
          <p>
            Parametrizacao #{{ consultaResultado.parametrizacao?.id }}
            -
            {{ consultaResultado.parametrizacao?.ativoDesejado?.simbolo || '?' }}/{{ consultaResultado.parametrizacao?.ativoPagamento?.simbolo || '?' }}
            via {{ consultaResultado.parametrizacao?.exchange?.nome || '-' }}
          </p>
        </div>
        <span class="result-time">{{ formatDate(consultaResultado.dataHoraConsulta) }}</span>
      </div>
      <div class="result-grid">
        <div class="result-metric buy">
          <span class="metric-label">Cotacao de Compra</span>
          <strong>{{ formatCotacao(consultaResultado.cotacaoCompra) }}</strong>
        </div>
        <div class="result-metric sell">
          <span class="metric-label">Cotacao de Venda</span>
          <strong>{{ formatCotacao(consultaResultado.cotacaoVenda) }}</strong>
        </div>
      </div>
    </div>

    <div class="glass-card">
      <div v-if="loading" class="loading-state">
        <span class="spinner"></span> Carregando parametrizacoes...
      </div>
      <div class="table-responsive" v-else>
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Exchange</th>
              <th>Rede</th>
              <th>Desejado</th>
              <th>Pagamento</th>
              <th>Identificador</th>
              <th>Qtd. Pagamento</th>
              <th>Status</th>
              <th class="actions-col">Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="parametrizacoes.length === 0">
              <td colspan="9" class="empty-state">Nenhuma parametrizacao cadastrada. Clique em "Nova Parametrizacao" para adicionar.</td>
            </tr>
            <tr v-for="p in parametrizacoes" :key="p.id">
              <td data-label="ID"><strong>#{{ p.id }}</strong></td>
              <td data-label="Exchange">{{ p.exchange?.nome || '-' }}</td>
              <td data-label="Rede">{{ p.rede?.nome || '-' }}</td>
              <td data-label="Desejado"><span class="token-badge buy">{{ p.ativoDesejado?.simbolo || '-' }}</span></td>
              <td data-label="Pagamento"><span class="token-badge sell">{{ p.ativoPagamento?.simbolo || '-' }}</span></td>
              <td data-label="Identificador">
                <span class="identifier-badge">{{ p.identificadorNegociacao || '-' }}</span>
              </td>
              <td data-label="Qtd. Pagamento">{{ p.quantidadePagamento }}</td>
              <td data-label="Status">
                <span :class="['status-badge', p.ativa ? 'active' : 'inactive']">
                  {{ p.ativa ? 'Ativa' : 'Inativa' }}
                </span>
              </td>
              <td data-label="Acoes" class="actions-col">
                <div class="action-buttons">
                  <button
                    @click="consultarPreco(p)"
                    class="icon-btn consult-btn"
                    :disabled="consultingId === p.id"
                    title="Consultar preco"
                  >
                    <svg v-if="consultingId !== p.id" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="m19 9-5 5-4-4-3 3"/></svg>
                    <span v-else class="inline-spinner"></span>
                  </button>
                  <button @click="openModal(p)" class="icon-btn edit-btn" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button @click="confirmDelete(p)" class="icon-btn delete-btn" title="Excluir">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content glass-card form-modal">
        <div class="modal-header">
          <h3>{{ editingId ? 'Editar Parametrizacao' : 'Nova Parametrizacao' }}</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        <div v-if="loadingOptions" class="loading-state" style="padding:1.5rem;">
          <span class="spinner"></span> Carregando opcoes...
        </div>
        <form v-else @submit.prevent="saveParametrizacao" class="modern-form">
          <div class="form-group">
            <label>Exchange *</label>
            <select v-model="form.exchangeId" required>
              <option value="" disabled>Selecione uma exchange</option>
              <option v-for="e in exchanges" :key="e.id" :value="e.id">{{ e.nome }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Rede *</label>
            <select v-model="form.redeId" required>
              <option value="" disabled>Selecione uma rede</option>
              <option v-for="r in redes" :key="r.id" :value="r.id">{{ r.nome }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group half-width">
              <label>Ativo Desejado *</label>
              <select v-model="form.ativoDesejadoId" required>
                <option value="" disabled>Selecione</option>
                <option v-for="a in ativos" :key="a.id" :value="a.id">{{ a.simbolo }} - {{ a.nome }}</option>
              </select>
            </div>
            <div class="form-group half-width">
              <label>Ativo de Pagamento *</label>
              <select v-model="form.ativoPagamentoId" required>
                <option value="" disabled>Selecione</option>
                <option v-for="a in ativos" :key="a.id" :value="a.id">{{ a.simbolo }} - {{ a.nome }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>Quantidade de Pagamento *</label>
            <input v-model.number="form.quantidadePagamento" type="number" step="any" min="0" placeholder="Ex: 100.0" required />
          </div>
          <div class="form-group">
            <label>Identificador de Negociacao *</label>
            <input
              v-model.trim="form.identificadorNegociacao"
              type="text"
              maxlength="100"
              placeholder="Ex: BTCUSDT"
              required
            />
            <small class="field-help">Codigo usado pela exchange para consultar cotacoes do par. Exemplo na Binance: BTCUSDT.</small>
          </div>
          <div class="form-row checks-row">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.ativa" />
              Parametrizacao Ativa
            </label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.logHabilitado" />
              Log Habilitado
            </label>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn primary-btn" :disabled="saving">
              {{ saving ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="isDeleteModalOpen" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content glass-card form-modal">
        <div class="modal-header">
          <h3 style="color: #ef4444;">Confirmar Exclusao</h3>
          <button @click="closeDeleteModal" class="close-btn">&times;</button>
        </div>
        <div style="margin-bottom: 1.5rem; color: #334155;">
          Tem certeza que deseja excluir a parametrizacao <strong>#{{ itemToDelete?.id }}</strong>?
          <p class="text-muted" style="margin-top: 0.5rem; font-size: 0.85rem;">Esta acao nao podera ser desfeita.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">Cancelar</button>
          <button class="btn primary-btn" style="background: linear-gradient(135deg, #ef4444, #b91c1c); box-shadow: 0 4px 6px rgba(239, 68, 68, 0.2);" @click="executeDelete">Excluir</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../utils/api.js'

const parametrizacoes = ref([])
const exchanges = ref([])
const redes = ref([])
const ativos = ref([])
const consultaResultado = ref(null)

const loading = ref(true)
const loadingOptions = ref(false)
const saving = ref(false)
const consultingId = ref(null)
const isModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const itemToDelete = ref(null)
const editingId = ref(null)

const defaultForm = {
  exchangeId: '',
  redeId: '',
  ativoDesejadoId: '',
  ativoPagamentoId: '',
  quantidadePagamento: '',
  identificadorNegociacao: '',
  ativa: true,
  logHabilitado: false
}
const form = ref({ ...defaultForm })
const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => (alertMessage.value = ''), 5000)
}

const formatCotacao = (value) => {
  if (value == null) return '-'
  return new Intl.NumberFormat('pt-BR', {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(value)
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('pt-BR')
}

const loadParametrizacoes = async () => {
  loading.value = true
  try {
    const res = await apiFetch('/api/parametrizacoes')
    parametrizacoes.value = res && res.data ? res.data : []
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar parametrizacoes', 'error')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  loadingOptions.value = true
  try {
    const [resEx, resRedes, resAtivos] = await Promise.all([
      apiFetch('/api/exchanges'),
      apiFetch('/api/redes'),
      apiFetch('/api/ativos')
    ])
    exchanges.value = resEx && resEx.data ? resEx.data : []
    redes.value = resRedes && resRedes.data ? resRedes.data : []
    ativos.value = resAtivos && resAtivos.data ? resAtivos.data : []
  } catch (error) {
    showAlert('Erro ao carregar opcoes do formulario', 'error')
  } finally {
    loadingOptions.value = false
  }
}

const openModal = async (p = null) => {
  alertMessage.value = ''
  await loadOptions()
  if (p) {
    editingId.value = p.id
    form.value = {
      exchangeId: p.exchange?.id || '',
      redeId: p.rede?.id || '',
      ativoDesejadoId: p.ativoDesejado?.id || '',
      ativoPagamentoId: p.ativoPagamento?.id || '',
      quantidadePagamento: p.quantidadePagamento,
      identificadorNegociacao: p.identificadorNegociacao || '',
      ativa: p.ativa,
      logHabilitado: p.logHabilitado
    }
  } else {
    editingId.value = null
    form.value = { ...defaultForm }
  }
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  editingId.value = null
}

const saveParametrizacao = async () => {
  saving.value = true
  try {
    const payload = {
      exchange: { id: form.value.exchangeId },
      rede: { id: form.value.redeId },
      ativoDesejado: { id: form.value.ativoDesejadoId },
      ativoPagamento: { id: form.value.ativoPagamentoId },
      quantidadePagamento: form.value.quantidadePagamento,
      identificadorNegociacao: form.value.identificadorNegociacao,
      ativa: form.value.ativa,
      logHabilitado: form.value.logHabilitado
    }
    const url = editingId.value ? `/api/parametrizacoes/${editingId.value}` : '/api/parametrizacoes'
    const method = editingId.value ? 'PUT' : 'POST'

    const res = await apiFetch(url, { method, body: JSON.stringify(payload) })
    if (res && res.error) throw new Error(res.message || 'Erro do servidor')

    showAlert(editingId.value ? 'Parametrizacao atualizada!' : 'Parametrizacao criada!', 'success')
    closeModal()
    await loadParametrizacoes()
  } catch (error) {
    showAlert(error.message || 'Erro ao salvar', 'error')
  } finally {
    saving.value = false
  }
}

const confirmDelete = (p) => {
  itemToDelete.value = p
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  itemToDelete.value = null
}

const executeDelete = async () => {
  if (!itemToDelete.value) return
  try {
    const res = await apiFetch(`/api/parametrizacoes/${itemToDelete.value.id}`, { method: 'DELETE' })
    if (res && res.error) throw new Error(res.message)
    showAlert('Parametrizacao excluida com sucesso.', 'success')
    closeDeleteModal()
    await loadParametrizacoes()
  } catch (error) {
    showAlert(error.message || 'Erro ao excluir.', 'error')
    closeDeleteModal()
  }
}

const consultarPreco = async (parametrizacao) => {
  if (!parametrizacao?.id) {
    showAlert('A parametrizacao precisa estar salva antes da consulta.', 'error')
    return
  }

  consultingId.value = parametrizacao.id
  try {
    const res = await apiFetch('/api/parametrizacoes/consultar-preco', {
      method: 'POST',
      body: JSON.stringify(parametrizacao)
    })

    if (res && res.error) throw new Error(res.message || 'Erro do servidor')

    consultaResultado.value = res && res.data ? res.data : null
    showAlert(`Consulta executada para a parametrizacao #${parametrizacao.id}.`, 'success')
  } catch (error) {
    showAlert(error.message || 'Erro ao consultar preco.', 'error')
  } finally {
    consultingId.value = null
  }
}

onMounted(() => {
  loadParametrizacoes()
})
</script>

<style scoped>
.param-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions h2 {
  margin: 0;
  color: #1e293b;
  font-size: 1.5rem;
  font-weight: 600;
}

.glass-card {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.03);
  border: 1px solid #f1f5f9;
}

.form-modal.glass-card {
  background: white;
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

.modern-table tbody tr { transition: background-color 0.2s; }
.modern-table tbody tr:hover { background-color: #f8fafc; }

.modern-table td strong {
  color: #0f172a;
  background: #e2e8f0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}

.empty-state {
  text-align: center;
  padding: 3rem !important;
  color: #94a3b8 !important;
  font-style: italic;
}

.text-muted { color: #94a3b8; }

.token-badge {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
}
.token-badge.buy { background: #dcfce7; color: #15803d; }
.token-badge.sell { background: #fee2e2; color: #b91c1c; }

.identifier-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 0.8rem;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

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

.result-card {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #f8fbff, #f8fffb);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.result-header h3 {
  margin: 0 0 0.35rem;
  color: #0f172a;
}

.result-header p {
  margin: 0;
  color: #475569;
  font-size: 0.92rem;
}

.result-time {
  color: #1d4ed8;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.result-metric {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  padding: 1rem 1.1rem;
  border-radius: 14px;
}

.result-metric.buy {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.result-metric.sell {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.metric-label {
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #64748b;
}

.result-metric strong {
  font-family: 'Courier New', monospace;
  font-size: 1.25rem;
  color: #0f172a;
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

.edit-btn { color: #3b82f6; }
.edit-btn:hover { background: #eff6ff; }
.consult-btn { color: #16a34a; }
.consult-btn:hover { background: #f0fdf4; }
.delete-btn { color: #ef4444; }
.delete-btn:hover { background: #fef2f2; }

.icon-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.inline-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(22, 163, 74, 0.2);
  border-top-color: #16a34a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.primary-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
  transition: transform 0.2s, box-shadow 0.2s;
}
.primary-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 12px rgba(37, 99, 235, 0.3); }
.primary-btn:disabled { opacity: 0.7; transform: none; cursor: not-allowed; }

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}
.btn-secondary { background: #f1f5f9; color: #475569; }
.btn-secondary:hover { background: #e2e8f0; }

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
  padding: 1rem;
}

.form-modal {
  width: 100%;
  max-width: 560px;
  padding: 2rem;
  animation: slideUp 0.3s ease-out;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.modal-header h3 { margin: 0; color: #0f172a; }
.close-btn { background: none; border: none; font-size: 1.5rem; color: #64748b; cursor: pointer; }

.modern-form { display: flex; flex-direction: column; gap: 1.2rem; }

.form-row { display: flex; gap: 1rem; }
.half-width { flex: 1; }

.checks-row {
  gap: 1.5rem;
  flex-wrap: wrap;
}

.form-group { display: flex; flex-direction: column; gap: 0.4rem; }
.form-group label { font-size: 0.85rem; font-weight: 600; color: #475569; }

.field-help {
  font-size: 0.78rem;
  color: #64748b;
  line-height: 1.4;
}

.form-group input,
.form-group select {
  padding: 0.75rem 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #1e293b;
  transition: all 0.2s;
  background: #f8fafc;
}
.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  font-weight: 500;
  color: #334155;
  cursor: pointer;
}
.checkbox-label input[type="checkbox"] { width: auto; accent-color: #2563eb; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.alert { padding: 1rem; border-radius: 8px; font-weight: 500; }
.alert.success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.alert.error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.alert.info { background: #e0f2fe; color: #075985; border: 1px solid #bae6fd; }

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
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 640px) {
  .header-actions { flex-direction: column; align-items: flex-start; gap: 1rem; }
  .header-actions h2 { font-size: 1.25rem; }
  .primary-btn { width: 100%; justify-content: center; }

  .glass-card { padding: 1rem; background: transparent; box-shadow: none; border: none; }
  .form-modal.glass-card {
    background: white;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.03);
    border: 1px solid #f1f5f9;
  }

  .table-responsive { overflow-x: visible; }

  .modern-table,
  .modern-table tbody,
  .modern-table tr,
  .modern-table td { display: block; width: 100%; }

  .modern-table thead { display: none; }

  .modern-table tr {
    margin-bottom: 1rem;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    background: white;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    overflow: hidden;
  }

  .modern-table td {
    text-align: right;
    padding: 1rem;
    border-bottom: 1px solid #f1f5f9;
    position: relative;
    padding-left: 50%;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    box-sizing: border-box;
  }

  .modern-table td:last-child { border-bottom: 0; background: #f8fafc; }

  .modern-table td::before {
    content: attr(data-label);
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    width: calc(50% - 1.5rem);
    text-align: left;
    font-weight: 600;
    color: #64748b;
    font-size: 0.8rem;
    text-transform: uppercase;
  }

  .actions-col { width: 100%; justify-content: flex-end !important; }
  .form-row { flex-direction: column; gap: 1.2rem; }
  .checks-row { flex-direction: column; gap: 0.75rem; }
  .result-header { flex-direction: column; }
  .result-time { white-space: normal; }
  .result-grid { grid-template-columns: 1fr; }
}
</style>
