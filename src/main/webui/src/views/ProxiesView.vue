<template>
  <div class="proxies-container">
    <div class="header-actions">
      <h2>Gerenciamento de Proxies</h2>
      <button @click="openModal()" class="primary-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
        Novo Proxy
      </button>
    </div>

    <!-- Alert / Error -->
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
        <span class="spinner"></span> Carregando proxies...
      </div>
      
      <div class="table-responsive" v-else>
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>URL</th>
              <th>Porta</th>
              <th>Usuário</th>
              <th class="actions-col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="proxies.length === 0">
              <td colspan="5" class="empty-state">Nenhum proxy cadastrado. Clique em "Novo Proxy" para adicionar.</td>
            </tr>
            <tr v-for="proxy in proxies" :key="proxy.id">
              <td data-label="ID"><strong>#{{ proxy.id }}</strong></td>
              <td data-label="Nome">{{ proxy.nome }}</td>
              <td data-label="URL">{{ proxy.url }}</td>
              <td data-label="Porta">{{ proxy.porta || '-' }}</td>
              <td data-label="Usuário">{{ proxy.usuario || '-' }}</td>
              <td data-label="Ações" class="actions-col">
                <div class="action-buttons">
                  <button @click="openModal(proxy)" class="icon-btn edit-btn" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button @click="confirmDelete(proxy)" class="icon-btn delete-btn" title="Excluir">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Formulario -->
    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content glass-card form-modal">
        <div class="modal-header">
          <h3>{{ editingId ? 'Editar Proxy' : 'Novo Proxy' }}</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        <form @submit.prevent="saveProxy" class="modern-form">
          <div class="form-group">
            <label>Nome *</label>
            <input v-model="form.nome" type="text" placeholder="Ex: Proxy Principal" required maxlength="100"/>
          </div>
          
          <div class="form-group">
            <label>URL *</label>
            <input v-model="form.url" type="text" placeholder="Ex: http://proxy.example.com:8080" required maxlength="500"/>
          </div>

          <div class="form-group">
            <label>Porta</label>
            <input v-model.number="form.porta" type="number" placeholder="Ex: 8080" />
          </div>

          <div class="form-row">
            <div class="form-group half-width">
              <label>Usuário</label>
              <input v-model="form.usuario" type="text" placeholder="Usuário (opcional)" maxlength="100"/>
            </div>
            
            <div class="form-group half-width">
              <label>Senha</label>
              <input v-model="form.senha" type="password" placeholder="Senha (opcional)" maxlength="100"/>
            </div>
          </div>

          <div class="form-group">
            <label>Token</label>
            <input v-model="form.token" type="password" placeholder="Token de Autenticação (opcional)" maxlength="500"/>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn primary-btn" :disabled="saving">
              {{ saving ? 'Salvando...' : 'Salvar Proxy' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Confirmação Exclusão -->
    <div v-if="isDeleteModalOpen" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content glass-card form-modal">
        <div class="modal-header">
          <h3 style="color: #ef4444;">Confirmar Exclusão</h3>
          <button @click="closeDeleteModal" class="close-btn">&times;</button>
        </div>
        <div style="margin-bottom: 1.5rem; color: #334155;">
          Tem certeza que deseja excluir o proxy <strong>"{{ itemToDelete?.nome }}"</strong>?
          <p class="text-muted" style="margin-top: 0.5rem; font-size: 0.85rem;">Esta ação não poderá ser desfeita.</p>
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

const proxies = ref([])
const loading = ref(true)
const saving = ref(false)

const isModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const itemToDelete = ref(null)
const editingId = ref(null)

const defaultForm = {
  nome: '',
  url: '',
  porta: null,
  usuario: '',
  senha: '',
  token: ''
}
const form = ref({ ...defaultForm })

const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => alertMessage.value = '', 5000)
}

const loadProxies = async () => {
  loading.value = true
  try {
    const res = await apiFetch('/api/proxies')
    if (res && res.data) {
      proxies.value = res.data
    } else {
      proxies.value = []
    }
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar proxies', 'error')
  } finally {
    loading.value = false
  }
}

const openModal = (proxy = null) => {
  alertMessage.value = ''
  if (proxy) {
    editingId.value = proxy.id
    form.value = {
      nome: proxy.nome,
      url: proxy.url,
      porta: proxy.porta || null,
      usuario: proxy.usuario || '',
      senha: proxy.senha || '',
      token: proxy.token || ''
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

const saveProxy = async () => {
  saving.value = true
  alertMessage.value = ''
  try {
    let url = '/api/proxies'
    let method = 'POST'
    
    if (editingId.value) {
      url = `/api/proxies/${editingId.value}`
      method = 'PUT'
    }

    const res = await apiFetch(url, {
      method,
      body: JSON.stringify(form.value)
    })
    
    if (res && res.error) {
      throw new Error(res.message || 'Erro do servidor')
    }

    showAlert(editingId.value ? 'Proxy atualizado com sucesso!' : 'Proxy criado com sucesso!', 'success')
    closeModal()
    await loadProxies()
    
  } catch (error) {
    showAlert(error.message || 'Erro ao salvar proxy', 'error')
  } finally {
    saving.value = false
  }
}

const confirmDelete = (proxy) => {
  itemToDelete.value = proxy
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  itemToDelete.value = null
}

const executeDelete = async () => {
  if (!itemToDelete.value) return
  try {
    const res = await apiFetch(`/api/proxies/${itemToDelete.value.id}`, { method: 'DELETE' })
    if (res && res.error) throw new Error(res.message)
    
    showAlert('Proxy excluído com sucesso.', 'success')
    closeDeleteModal()
    await loadProxies()
  } catch (error) {
    showAlert(error.message || 'Erro ao excluir.', 'error')
    closeDeleteModal()
  }
}

onMounted(() => {
  loadProxies()
})
</script>

<style scoped>
.proxies-container {
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

/* Glass Card Global Modifier */
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

/* Tabela Moderna */
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

.empty-state {
  text-align: center;
  padding: 3rem !important;
  color: #94a3b8 !important;
  font-style: italic;
}

.text-muted { color: #94a3b8; }

/* Ações / Botões */
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

.edit-btn { color: #3b82f6; }
.edit-btn:hover { background: #eff6ff; }
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
.btn-secondary:hover { background: #e2e8f0; }

/* Modais */
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
}

.form-modal {
  width: 100%;
  max-width: 500px;
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

/* Formularios Modernos */
.modern-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}
.half-width {
  flex: 1;
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

.form-group input, .form-group select {
  padding: 0.75rem 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #1e293b;
  transition: all 0.2s;
  background: #f8fafc;
}
.form-group input:focus, .form-group select:focus {
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

/* Utilitarios */
.alert {
  padding: 1rem;
  border-radius: 8px;
  font-weight: 500;
}
.alert.success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; box-shadow: 0 4px 6px -1px rgba(22, 163, 74, 0.2); }
.alert.error { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; box-shadow: 0 4px 6px -1px rgba(239, 68, 68, 0.2); }
.alert.info { background: #e0f2fe; color: #075985; border: 1px solid #bae6fd; box-shadow: 0 4px 6px -1px rgba(14, 165, 233, 0.2); }

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

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(100%); }
  to { opacity: 1; transform: translateX(0); }
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
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
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

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

/* Mobile Card Layout for Table */
@media (max-width: 640px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
    width: 100%;
  }

  .header-actions h2 { font-size: 1.25rem; width: 100%; }
  
  .header-actions button, .primary-btn, .secondary-btn {
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

  .table-responsive { overflow-x: visible; }

  .modern-table, 
  .modern-table tbody, 
  .modern-table tr, 
  .modern-table td {
    display: block;
    width: 100%;
  }

  .modern-table thead { display: none; }

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

  .actions-col {
    width: 100%;
    justify-content: flex-end !important;
  }

  .form-row {
    flex-direction: column;
    gap: 1.2rem;
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
