<template>
  <div class="ativos-redes-container">
    <div class="header-actions">
      <h2>Configuracao Ativo Rede</h2>
      <button @click="openModal()" class="primary-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
        Nova Configuracao
      </button>
    </div>

    <div v-if="alertMessage" :class="['alert', alertType, 'floating-alert']">
      <div class="alert-content">
        <svg v-if="alertType === 'success'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else-if="alertType === 'error'" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <span>{{ alertMessage }}</span>
      </div>
      <button @click="alertMessage = ''" class="alert-close">&times;</button>
    </div>

    <div class="glass-card">
      <div v-if="loading" class="loading-state">
        <span class="spinner"></span> Carregando configuracoes...
      </div>

      <div class="table-responsive" v-else>
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Ativo</th>
              <th>Rede</th>
              <th>Identificador</th>
              <th>Casas Decimais</th>
              <th class="actions-col">Acoes</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="configuracoes.length === 0">
              <td colspan="6" class="empty-state">Nenhuma configuracao cadastrada. Clique em "Nova Configuracao" para adicionar.</td>
            </tr>
            <tr v-for="configuracao in configuracoes" :key="configuracao.id">
              <td data-label="ID"><strong>#{{ configuracao.id }}</strong></td>
              <td data-label="Ativo">
                <div class="asset-cell">
                  <span class="asset-symbol">{{ configuracao.ativoFinanceiro?.simbolo || '-' }}</span>
                  <span class="asset-name">{{ configuracao.ativoFinanceiro?.nome || '-' }}</span>
                </div>
              </td>
              <td data-label="Rede">{{ configuracao.rede?.nome || '-' }}</td>
              <td data-label="Identificador">
                <span class="identifier-badge">{{ configuracao.identificador }}</span>
              </td>
              <td data-label="Casas Decimais">{{ configuracao.quantidadeCasasDecimais ?? 6 }}</td>
              <td data-label="Acoes" class="actions-col">
                <div class="action-buttons">
                  <button @click="openModal(configuracao)" class="icon-btn edit-btn" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button @click="cloneConfiguracao(configuracao)" class="icon-btn clone-btn" title="Clonar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                  </button>
                  <button @click="confirmDelete(configuracao)" class="icon-btn delete-btn" title="Excluir">
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
          <h3>{{ modalMode === 'edit' ? 'Editar Configuracao' : modalMode === 'clone' ? 'Clonar Configuracao' : 'Nova Configuracao' }}</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        <div v-if="loadingOptions" class="loading-state" style="padding:1.5rem;">
          <span class="spinner"></span> Carregando opcoes...
        </div>
        <form v-else @submit.prevent="saveConfiguracao" class="modern-form">
          <div class="form-group">
            <label>Ativo *</label>
            <select v-model="form.ativoFinanceiroId" required>
              <option value="" disabled>Selecione um ativo</option>
              <option v-for="ativo in ativos" :key="ativo.id" :value="ativo.id">
                {{ ativo.simbolo }} - {{ ativo.nome }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Rede *</label>
            <select v-model="form.redeId" required>
              <option value="" disabled>Selecione uma rede</option>
              <option v-for="rede in redes" :key="rede.id" :value="rede.id">
                {{ rede.nome }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Identificador *</label>
            <input
              v-model.trim="form.identificador"
              type="text"
              maxlength="150"
              placeholder="Ex: BTC, ETH, USDT"
              required
            />
            <small class="field-help">Codigo ou identificador do ativo naquela rede.</small>
          </div>
          <div class="form-group">
            <label>Quantidade de Casas Decimais *</label>
            <input
              v-model.number="form.quantidadeCasasDecimais"
              type="number"
              min="0"
              step="1"
              placeholder="6"
              required
            />
            <small class="field-help">Numero inteiro usado para formatar valores desse ativo na rede.</small>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn primary-btn" :disabled="saving">
              {{ saving ? 'Salvando...' : modalMode === 'edit' ? 'Salvar Alteracoes' : modalMode === 'clone' ? 'Salvar como Nova' : 'Salvar Configuracao' }}
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
        <div class="delete-copy">
          Tem certeza que deseja excluir a configuracao do ativo
          <strong>{{ itemToDelete?.ativoFinanceiro?.simbolo }}</strong>
          na rede
          <strong>{{ itemToDelete?.rede?.nome }}</strong>?
          <p class="text-muted">Esta acao nao podera ser desfeita.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">Cancelar</button>
          <button class="btn primary-btn danger-btn" @click="executeDelete">Excluir</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '../utils/api.js'

const configuracoes = ref([])
const ativos = ref([])
const redes = ref([])

const loading = ref(true)
const loadingOptions = ref(false)
const saving = ref(false)

const isModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const itemToDelete = ref(null)
const editingId = ref(null)
const modalMode = ref('create')

const defaultForm = {
  ativoFinanceiroId: '',
  redeId: '',
  identificador: '',
  quantidadeCasasDecimais: 6
}

const form = ref({ ...defaultForm })

const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => {
    alertMessage.value = ''
  }, 5000)
}

const mapConfiguracaoToForm = (configuracao) => ({
  ativoFinanceiroId: configuracao.ativoFinanceiro?.id || '',
  redeId: configuracao.rede?.id || '',
  identificador: configuracao.identificador || '',
  quantidadeCasasDecimais: configuracao.quantidadeCasasDecimais ?? 6
})

const loadConfiguracoes = async () => {
  loading.value = true
  try {
    const res = await apiFetch('/api/ativos-redes')
    configuracoes.value = res && res.data ? res.data : []
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar configuracoes ativo rede', 'error')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  loadingOptions.value = true
  try {
    const [resAtivos, resRedes] = await Promise.all([
      apiFetch('/api/ativos'),
      apiFetch('/api/redes')
    ])

    ativos.value = resAtivos && resAtivos.data ? resAtivos.data : []
    redes.value = resRedes && resRedes.data ? resRedes.data : []
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar opcoes do formulario', 'error')
  } finally {
    loadingOptions.value = false
  }
}

const openModal = async (configuracao = null) => {
  alertMessage.value = ''
  await loadOptions()

  if (configuracao) {
    modalMode.value = 'edit'
    editingId.value = configuracao.id
    form.value = mapConfiguracaoToForm(configuracao)
  } else {
    modalMode.value = 'create'
    editingId.value = null
    form.value = { ...defaultForm }
  }

  isModalOpen.value = true
}

const cloneConfiguracao = async (configuracao) => {
  alertMessage.value = ''
  await loadOptions()
  modalMode.value = 'clone'
  editingId.value = null
  form.value = mapConfiguracaoToForm(configuracao)
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  editingId.value = null
  modalMode.value = 'create'
}

const saveConfiguracao = async () => {
  saving.value = true
  try {
    const payload = {
      ativoFinanceiro: { id: form.value.ativoFinanceiroId },
      rede: { id: form.value.redeId },
      identificador: form.value.identificador,
      quantidadeCasasDecimais: form.value.quantidadeCasasDecimais ?? 6
    }

    const url = editingId.value ? `/api/ativos-redes/${editingId.value}` : '/api/ativos-redes'
    const method = editingId.value ? 'PUT' : 'POST'

    const res = await apiFetch(url, {
      method,
      body: JSON.stringify(payload)
    })

    if (res && res.error) {
      throw new Error(res.message || 'Erro do servidor')
    }

    const successMessage = editingId.value
      ? 'Configuracao atualizada com sucesso!'
      : modalMode.value === 'clone'
        ? 'Configuracao clonada e criada com sucesso!'
        : 'Configuracao criada com sucesso!'

    showAlert(successMessage, 'success')
    closeModal()
    await loadConfiguracoes()
  } catch (error) {
    showAlert(error.message || 'Erro ao salvar configuracao ativo rede', 'error')
  } finally {
    saving.value = false
  }
}

const confirmDelete = (configuracao) => {
  itemToDelete.value = configuracao
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  itemToDelete.value = null
}

const executeDelete = async () => {
  if (!itemToDelete.value) return

  try {
    const res = await apiFetch(`/api/ativos-redes/${itemToDelete.value.id}`, { method: 'DELETE' })
    if (res && res.error) {
      throw new Error(res.message)
    }

    showAlert('Configuracao excluida com sucesso.', 'success')
    closeDeleteModal()
    await loadConfiguracoes()
  } catch (error) {
    showAlert(error.message || 'Erro ao excluir configuracao ativo rede', 'error')
    closeDeleteModal()
  }
}

onMounted(async () => {
  await Promise.all([loadConfiguracoes(), loadOptions()])
})
</script>

<style scoped>
.ativos-redes-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
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

.table-responsive {
  overflow-x: auto;
}

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

.modern-table tbody tr {
  transition: background-color 0.2s;
}

.modern-table tbody tr:hover {
  background-color: #f8fafc;
}

.modern-table td strong {
  color: #0f172a;
  background: #e2e8f0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}

.asset-cell {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.asset-symbol {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 0.8rem;
  font-weight: 700;
}

.asset-name {
  color: #334155;
}

.identifier-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;
  font-size: 0.82rem;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.empty-state {
  text-align: center;
  padding: 3rem !important;
  color: #94a3b8 !important;
  font-style: italic;
}

.text-muted {
  color: #94a3b8;
}

.field-help {
  font-size: 0.78rem;
  color: #64748b;
  line-height: 1.4;
}

.delete-copy {
  margin-bottom: 1.5rem;
  color: #334155;
}

.delete-copy p {
  margin-top: 0.5rem;
  font-size: 0.85rem;
}

.actions-col {
  width: 100px;
  text-align: center;
}

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

.edit-btn {
  color: #3b82f6;
}

.edit-btn:hover {
  background: #eff6ff;
}

.clone-btn {
  color: #8b5cf6;
}

.clone-btn:hover {
  background: #f5f3ff;
}

.delete-btn {
  color: #ef4444;
}

.delete-btn:hover {
  background: #fef2f2;
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

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.3);
}

.primary-btn:disabled {
  opacity: 0.7;
  transform: none;
  cursor: not-allowed;
}

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.danger-btn {
  background: linear-gradient(135deg, #ef4444, #b91c1c);
  box-shadow: 0 4px 6px rgba(239, 68, 68, 0.2);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
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
  max-width: 520px;
  padding: 2rem;
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  color: #0f172a;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #64748b;
  cursor: pointer;
}

.modern-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
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

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.alert {
  padding: 1rem;
  border-radius: 8px;
  font-weight: 500;
}

.alert.success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
  box-shadow: 0 4px 6px -1px rgba(22, 163, 74, 0.2);
}

.alert.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
  box-shadow: 0 4px 6px -1px rgba(239, 68, 68, 0.2);
}

.alert.info {
  background: #e0f2fe;
  color: #075985;
  border: 1px solid #bae6fd;
  box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2);
}

.floating-alert {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 2000;
  min-width: 300px;
  max-width: 450px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  animation: slideInRight 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.alert-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  line-height: 1;
  color: currentColor;
  opacity: 0.6;
  cursor: pointer;
  transition: opacity 0.2s;
  padding: 0;
}

.alert-close:hover {
  opacity: 1;
}

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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 640px) {
  .floating-alert {
    top: 1rem;
    right: 1rem;
    left: 1rem;
    min-width: auto;
    max-width: none;
    animation: slideDown 0.3s ease-out;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
    width: 100%;
  }

  .header-actions h2 {
    font-size: 1.25rem;
    width: 100%;
  }

  .header-actions button,
  .primary-btn,
  .secondary-btn {
    width: 100%;
    justify-content: center;
  }

  .glass-card {
    padding: 1rem;
    background: transparent;
    box-shadow: none;
    border: none;
  }

  .form-modal.glass-card {
    background: white;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.03);
    border: 1px solid #f1f5f9;
  }

  .table-responsive {
    overflow-x: visible;
  }

  .modern-table,
  .modern-table tbody,
  .modern-table tr,
  .modern-table td {
    display: block;
    width: 100%;
  }

  .modern-table thead {
    display: none;
  }

  .modern-table tr {
    margin-bottom: 1rem;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 0;
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

  .modern-table td:last-child {
    border-bottom: 0;
    background: #f8fafc;
  }

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

  .asset-cell {
    flex-direction: column;
    align-items: flex-end;
  }

  .actions-col {
    width: 100%;
    justify-content: flex-end !important;
  }
}

.header-actions,
.header-buttons {
  flex-direction: column;
  gap: 0.75rem;
  justify-content: initial;
  align-items: stretch;
}

.header-actions > *,
.header-buttons > * {
  width: 100%;
}

.header-actions :deep(.p-button),
.header-actions button,
.header-buttons :deep(.p-button),
.header-buttons button {
  width: 100%;
}
</style>
