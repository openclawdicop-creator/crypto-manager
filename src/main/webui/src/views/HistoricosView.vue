<template>
  <div class="historicos-container">
    <div class="header-actions">
      <h2>Historico de Cotacoes</h2>
      <div class="header-right">
        <select v-model="filtroParametrizacaoId" @change="loadHistoricos" class="filter-select">
          <option value="">Todos</option>
          <option v-for="p in parametrizacoes" :key="p.id" :value="p.id">
            #{{ p.id }} - {{ p.ativoDesejado?.simbolo }}/{{ p.ativoPagamento?.simbolo }} ({{ p.exchange?.nome }})
          </option>
        </select>
      </div>
    </div>

    <div v-if="alertMessage" :class="['alert', alertType]">
      {{ alertMessage }}
    </div>

    <div class="glass-card">
      <div v-if="loading" class="loading-state">
        <span class="spinner"></span> Carregando historico...
      </div>
      <div class="table-responsive" v-else>
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Parametrizacao</th>
              <th>Par de Tokens</th>
              <th>Cotacao Compra</th>
              <th>Cotacao Venda</th>
              <th>Data / Hora</th>
              <th class="actions-col">Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="historicos.length === 0">
              <td colspan="7" class="empty-state">Nenhum historico de cotacao encontrado.</td>
            </tr>
            <tr v-for="h in historicos" :key="h.id">
              <td data-label="ID"><strong>#{{ h.id }}</strong></td>
              <td data-label="Parametrizacao">
                <span class="param-ref">#{{ h.parametrizacao?.id }}</span>
                {{ h.parametrizacao?.exchange?.nome || '' }}
              </td>
              <td data-label="Par de Tokens">
                <span class="token-badge buy">{{ h.parametrizacao?.ativoDesejado?.simbolo || '?' }}</span>
                <span class="pair-separator">/</span>
                <span class="token-badge sell">{{ h.parametrizacao?.ativoPagamento?.simbolo || '?' }}</span>
              </td>
              <td data-label="Cotacao Compra"><strong class="cotacao-value">{{ formatCotacao(h.cotacaoCompra) }}</strong></td>
              <td data-label="Cotacao Venda"><strong class="cotacao-value">{{ formatCotacao(h.cotacaoVenda) }}</strong></td>
              <td data-label="Data / Hora">{{ formatDate(h.dataHoraConsulta) }}</td>
              <td data-label="Acoes" class="actions-col">
                <div class="action-buttons">
                  <button @click="confirmDelete(h)" class="icon-btn delete-btn" title="Excluir">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isDeleteModalOpen" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content glass-card form-modal">
        <div class="modal-header">
          <h3 style="color: #ef4444;">Confirmar Exclusao</h3>
          <button @click="closeDeleteModal" class="close-btn">&times;</button>
        </div>
        <div style="margin-bottom: 1.5rem; color: #334155;">
          Tem certeza que deseja excluir o registro de cotacao <strong>#{{ itemToDelete?.id }}</strong>?
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

const historicos = ref([])
const parametrizacoes = ref([])
const loading = ref(true)
const isDeleteModalOpen = ref(false)
const itemToDelete = ref(null)
const filtroParametrizacaoId = ref('')
const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => alertMessage.value = '', 5000)
}

const formatCotacao = (v) => {
  if (v == null) return '-'
  return new Intl.NumberFormat('pt-BR', {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(v)
}

const formatDate = (dt) => {
  if (!dt) return '-'
  const d = new Date(dt)
  return d.toLocaleString('pt-BR')
}

const loadHistoricos = async () => {
  loading.value = true
  try {
    const url = filtroParametrizacaoId.value
      ? `/api/historicos/parametrizacao/${filtroParametrizacaoId.value}`
      : '/api/historicos'
    const res = await apiFetch(url)
    historicos.value = (res && res.data) ? res.data : []
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar historico', 'error')
  } finally {
    loading.value = false
  }
}

const loadParametrizacoes = async () => {
  try {
    const res = await apiFetch('/api/parametrizacoes')
    parametrizacoes.value = (res && res.data) ? res.data : []
  } catch (_) {}
}

const confirmDelete = (h) => {
  itemToDelete.value = h
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  itemToDelete.value = null
}

const executeDelete = async () => {
  if (!itemToDelete.value) return
  try {
    const res = await apiFetch(`/api/historicos/${itemToDelete.value.id}`, { method: 'DELETE' })
    if (res && res.error) throw new Error(res.message)
    showAlert('Registro excluido com sucesso.', 'success')
    closeDeleteModal()
    await loadHistoricos()
  } catch (error) {
    showAlert(error.message || 'Erro ao excluir.', 'error')
    closeDeleteModal()
  }
}

onMounted(async () => {
  await Promise.all([loadParametrizacoes(), loadHistoricos()])
})
</script>

<style scoped>
.historicos-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-actions h2 {
  margin: 0;
  color: #1e293b;
  font-size: 1.5rem;
  font-weight: 600;
}

.filter-select {
  padding: 0.6rem 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #1e293b;
  background: white;
  cursor: pointer;
  min-width: 240px;
  transition: all 0.2s;
}
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
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

.param-ref {
  font-size: 0.8rem;
  font-weight: 700;
  background: #e2e8f0;
  color: #475569;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  margin-right: 0.3rem;
}

.token-badge {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
}
.token-badge.buy { background: #dcfce7; color: #15803d; }
.token-badge.sell { background: #fee2e2; color: #b91c1c; }

.pair-separator {
  margin: 0 0.2rem;
  color: #94a3b8;
  font-weight: 600;
}

.cotacao-value {
  color: #0f172a;
  background: transparent !important;
  font-family: 'Courier New', monospace;
}

.actions-col { width: 80px; text-align: center; }

.action-buttons { display: flex; justify-content: center; gap: 0.5rem; }

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

.delete-btn { color: #ef4444; }
.delete-btn:hover { background: #fef2f2; }

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
}

.btn { padding: 0.6rem 1.2rem; border-radius: 8px; font-weight: 600; cursor: pointer; border: none; }
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
  max-width: 480px;
  padding: 2rem;
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.modal-header h3 { margin: 0; color: #0f172a; }
.close-btn { background: none; border: none; font-size: 1.5rem; color: #64748b; cursor: pointer; }

.modal-footer { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1rem; }

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
  .filter-select { width: 100%; min-width: unset; }

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
    gap: 0.3rem;
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
}
</style>
