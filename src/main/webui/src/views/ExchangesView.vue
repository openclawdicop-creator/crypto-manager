<template>
  <div class="exchanges-container">
    <div class="header-actions">
      <h2>Gerenciamento de Exchanges</h2>
      <button @click="openModal()" class="primary-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
        Nova Exchange
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
        <span class="spinner"></span> Carregando exchanges...
      </div>
      
      <div class="table-responsive" v-else>
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Tipo</th>
              <th>Tipo API</th>
              <th>Categoria</th>
              <th>Profundidade</th>
              <th>Proxy</th>
              <th class="actions-col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="exchanges.length === 0">
              <td colspan="8" class="empty-state">Nenhuma exchange cadastrada. Clique em "Nova Exchange" para adicionar.</td>
            </tr>
            <tr v-for="exchange in exchanges" :key="exchange.id">
              <td data-label="ID"><strong>#{{ exchange.id }}</strong></td>
              <td data-label="Nome">{{ exchange.nome }}</td>
              <td data-label="Tipo">{{ exchange.tipo }}</td>
              <td data-label="Tipo API"><span :class="['api-badge', exchange.tipoApi]">{{ exchange.tipoApi }}</span></td>
              <td data-label="Categoria">
                <span :class="['category-badge', exchange.categoria || 'SPOT']">
                  {{ formatCategoria(exchange.categoria) }}
                </span>
              </td>
              <td data-label="Profundidade">
                <span class="depth-badge">{{ exchange.profundidadeLivroOfertas || 10 }}</span>
              </td>
              <td data-label="Proxy">
                <span :class="['status-badge', exchange.usarProxy ? 'active' : 'inactive']">
                  {{ exchange.usarProxy ? 'Sim' : 'Não' }}
                </span>
              </td>
              <td data-label="Ações" class="actions-col">
                <div class="action-buttons">
                  <button @click="openModal(exchange)" class="icon-btn edit-btn" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  </button>
                  <button @click="confirmDelete(exchange)" class="icon-btn delete-btn" title="Excluir">
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
          <h3>{{ editingId ? 'Editar Exchange' : 'Nova Exchange' }}</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        <form @submit.prevent="saveExchange" class="modern-form">
          <div class="form-group">
            <label>Nome *</label>
            <input v-model="form.nome" type="text" placeholder="Ex: Binance" required maxlength="100"/>
          </div>
          
          <div class="form-group">
            <label>Descricao</label>
            <input v-model="form.descricao" type="text" placeholder="Sobre a corretora" maxlength="500"/>
          </div>

          <div class="form-row">
            <div class="form-group half-width">
              <label>Tipo *</label>
              <select v-model="form.tipo" required>
                <option value="CENTRALIZADA">Centralizada (CEX)</option>
                <option value="DESCENTRALIZADA">Descentralizada (DEX)</option>
              </select>
            </div>
            
            <div class="form-group half-width">
              <label>Tipo de API *</label>
              <select v-model="form.tipoApi" required>
                <option value="REST">REST</option>
                <option value="WEBSOCKET">WebSocket</option>
              </select>
            </div>

            <div class="form-group half-width">
              <label>Categoria *</label>
              <select v-model="form.categoria" required>
                <option value="SPOT">Spot</option>
                <option value="FUTURO">Futuro</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>URL da API</label>
            <input v-model="form.urlApi" type="url" placeholder="Ex: https://api.binance.com" maxlength="200"/>
          </div>

          <div class="form-group">
            <label>Profundidade do livro de ofertas *</label>
            <input
              v-model.number="form.profundidadeLivroOfertas"
              type="number"
              min="1"
              step="1"
              placeholder="Ex: 10"
              required
            />
          </div>

          <div class="form-group">
            <label>Token da API</label>
            <input v-model="form.tokenApi" type="password" placeholder="Chave de Acesso (Se aplicável)" maxlength="500"/>
          </div>

          <div class="form-group checks">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.logHabilitado"/>
              Habilitar logs de diagnóstico para esta exchange
            </label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.usarProxy"/>
              Usar Proxy para conexões com esta exchange
            </label>
          </div>
          
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Cancelar</button>
            <button type="submit" class="btn primary-btn" :disabled="saving">
              {{ saving ? 'Salvando...' : 'Salvar Exchange' }}
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
          Tem certeza que deseja excluir a exchange <strong>"{{ itemToDelete?.nome }}"</strong>?
          <p class="text-muted" style="margin-top: 0.5rem; font-size: 0.85rem;">Esta ação não poderá ser desfeita e pode afetar as parametrizações existentes.</p>
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

const exchanges = ref([])
const loading = ref(true)
const saving = ref(false)

const isModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const itemToDelete = ref(null)
const editingId = ref(null)

const defaultForm = {
  nome: '',
  descricao: '',
  tipo: 'CENTRALIZADA',
  tipoApi: 'REST',
  categoria: 'SPOT',
  urlApi: '',
  profundidadeLivroOfertas: 10,
  tokenApi: '',
  logHabilitado: false,
  usarProxy: false
}
const form = ref({ ...defaultForm })

const alertMessage = ref('')
const alertType = ref('info')

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => alertMessage.value = '', 5000)
}

const formatCategoria = (categoria) => {
  if (categoria === 'FUTURO') return 'Futuro'
  return 'Spot'
}

const loadExchanges = async () => {
  loading.value = true
  try {
    const res = await apiFetch('/api/exchanges')
    if (res && res.data) {
      exchanges.value = res.data
    } else {
      exchanges.value = []
    }
  } catch (error) {
    showAlert(error.message || 'Erro ao carregar exchanges', 'error')
  } finally {
    loading.value = false
  }
}

const openModal = (exchange = null) => {
  alertMessage.value = ''
  if (exchange) {
    editingId.value = exchange.id
    form.value = {
      nome: exchange.nome,
      descricao: exchange.descricao || '',
      tipo: exchange.tipo || 'CENTRALIZADA',
      tipoApi: exchange.tipoApi || 'REST',
      categoria: exchange.categoria || 'SPOT',
      urlApi: exchange.urlApi || '',
      profundidadeLivroOfertas: exchange.profundidadeLivroOfertas || 10,
      tokenApi: exchange.tokenApi || '',
      logHabilitado: exchange.logHabilitado || false,
      usarProxy: exchange.usarProxy || false
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

const saveExchange = async () => {
  saving.value = true
  alertMessage.value = ''
  try {
    let url = '/api/exchanges'
    let method = 'POST'
    
    if (editingId.value) {
      url = `/api/exchanges/${editingId.value}`
      method = 'PUT'
    }

    const res = await apiFetch(url, {
      method,
      body: JSON.stringify(form.value)
    })
    
    if (res && res.error) {
      throw new Error(res.message || 'Erro do servidor')
    }

    showAlert(editingId.value ? 'Exchange atualizada com sucesso!' : 'Exchange criada com sucesso!', 'success')
    closeModal()
    await loadExchanges()
    
  } catch (error) {
    showAlert(error.message || 'Erro ao salvar exchange', 'error')
  } finally {
    saving.value = false
  }
}

const confirmDelete = (exchange) => {
  itemToDelete.value = exchange
  isDeleteModalOpen.value = true
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  itemToDelete.value = null
}

const executeDelete = async () => {
  if (!itemToDelete.value) return
  try {
    const res = await apiFetch(`/api/exchanges/${itemToDelete.value.id}`, { method: 'DELETE' })
    if (res && res.error) throw new Error(res.message)
    
    showAlert('Exchange excluída com sucesso.', 'success')
    closeDeleteModal()
    await loadExchanges()
  } catch (error) {
    showAlert(error.message || 'Erro ao excluir.', 'error')
    closeDeleteModal()
  }
}

onMounted(() => {
  loadExchanges()
})
</script>

<style scoped>
.exchanges-container {
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

.api-badge {
  font-size: 0.8rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
}
.api-badge.REST { background: #dcfce7; color: #15803d; }
.api-badge.WEBSOCKET { background: #e0e7ff; color: #4338ca; }

.category-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 700;
}
.category-badge.SPOT { background: #dbeafe; color: #1d4ed8; }
.category-badge.FUTURO { background: #fee2e2; color: #b91c1c; }

.depth-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  background: #fef3c7;
  color: #92400e;
  font-size: 0.8rem;
  font-weight: 700;
}

.status-badge {
  font-size: 0.8rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
}
.status-badge.active { background: #dcfce7; color: #15803d; }
.status-badge.inactive { background: #f1f5f9; color: #64748b; }

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

.checks {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-top: 0.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: normal;
  cursor: pointer;
  color: #475569;
  font-size: 0.9rem;
}
.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
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
