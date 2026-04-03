<template>
  <div class="sql-manager-container">
    <div class="header-card glass-card">
      <div>
        <h2>SQL Manager</h2>
        <p>Execute comandos SQL diretamente no banco da aplicacao.</p>
      </div>
    </div>

    <div v-if="alertMessage" :class="['alert', alertType]">
      {{ alertMessage }}
    </div>

    <div class="glass-card editor-card">
      <label class="editor-label" for="sql-editor">Comando SQL</label>
      <textarea
        id="sql-editor"
        v-model="sql"
        class="sql-editor"
        placeholder="Ex: SELECT * FROM usuario ORDER BY id DESC"
      ></textarea>

      <div class="actions">
        <button class="primary-btn" :disabled="executing" @click="executeSql">
          {{ executing ? 'Executando...' : 'Executar' }}
        </button>
        <button class="secondary-btn" :disabled="executing" @click="clearSql">
          Limpar
        </button>
      </div>
    </div>

    <div v-if="result" class="glass-card result-card">
      <div class="result-header">
        <h3>Resultado</h3>
        <span class="result-badge">{{ result.executionType }}</span>
      </div>

      <div class="result-summary">
        <span v-if="result.executionType === 'RESULT_SET'">
          {{ result.rowCount || 0 }} registro(s) retornado(s)
        </span>
        <span v-else>
          {{ result.updateCount ?? 0 }} linha(s) afetada(s)
        </span>
      </div>

      <div v-if="result.executionType === 'RESULT_SET'" class="table-responsive">
        <table v-if="result.rows?.length" class="modern-table">
          <thead>
            <tr>
              <th v-for="column in result.columns" :key="column">{{ column }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in result.rows" :key="index">
              <td v-for="column in result.columns" :key="`${index}-${column}`">
                {{ formatCell(row[column]) }}
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state">A consulta nao retornou registros.</div>
      </div>

      <pre v-else class="result-json">{{ formattedResult }}</pre>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { apiFetch } from '../utils/api.js'

const sql = ref('')
const executing = ref(false)
const result = ref(null)
const alertMessage = ref('')
const alertType = ref('info')

const formattedResult = computed(() => JSON.stringify(result.value, null, 2))

const showAlert = (message, type = 'info') => {
  alertMessage.value = message
  alertType.value = type
}

const clearSql = () => {
  sql.value = ''
}

const executeSql = async () => {
  if (!sql.value.trim()) {
    showAlert('Informe um SQL para executar.', 'error')
    return
  }

  executing.value = true
  alertMessage.value = ''

  try {
    const response = await apiFetch('/api/sql/execute', {
      method: 'POST',
      body: JSON.stringify({ sql: sql.value })
    })

    if (response?.error) {
      throw new Error(response.message || 'Erro ao executar SQL.')
    }

    result.value = response.data
    showAlert(response.message || 'SQL executado com sucesso.', 'success')
  } catch (error) {
    result.value = null
    showAlert(error.message || 'Erro ao executar SQL.', 'error')
  } finally {
    executing.value = false
  }
}

const formatCell = (value) => {
  if (value == null) return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return value
}
</script>

<style scoped>
.sql-manager-container {
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

.header-card h2,
.result-header h3 {
  margin: 0;
  color: #0f172a;
}

.header-card p {
  margin: 0.5rem 0 0;
  color: #64748b;
}

.editor-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.editor-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #475569;
}

.sql-editor {
  min-height: 280px;
  width: 100%;
  resize: vertical;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 1rem;
  font-size: 0.95rem;
  line-height: 1.6;
  color: #0f172a;
  background: #f8fafc;
  font-family: 'Courier New', monospace;
}

.sql-editor:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
  background: #ffffff;
}

.actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.primary-btn,
.secondary-btn {
  border: none;
  border-radius: 8px;
  padding: 0.75rem 1.4rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-btn {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  color: white;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

.secondary-btn {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.alert {
  padding: 1rem 1.25rem;
  border-radius: 10px;
  font-weight: 500;
}

.alert.success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.alert.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.alert.info {
  background: #e0f2fe;
  color: #075985;
  border: 1px solid #bae6fd;
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.result-badge {
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 999px;
  padding: 0.3rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
}

.result-summary {
  color: #475569;
  font-weight: 500;
}

.table-responsive {
  overflow-x: auto;
}

.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.modern-table th {
  background: #f8fafc;
  color: #64748b;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.875rem 1rem;
  border-bottom: 2px solid #e2e8f0;
  text-align: left;
}

.modern-table td {
  padding: 0.875rem 1rem;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  vertical-align: top;
  white-space: nowrap;
}

.empty-state {
  padding: 1.5rem;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  text-align: center;
  color: #64748b;
  background: #f8fafc;
}

.result-json {
  margin: 0;
  padding: 1rem;
  border-radius: 12px;
  background: #0f172a;
  color: #e2e8f0;
  overflow-x: auto;
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .glass-card {
    padding: 1rem;
  }

  .sql-editor {
    min-height: 220px;
  }

  .actions {
    flex-direction: column;
  }

  .primary-btn,
  .secondary-btn {
    width: 100%;
  }
}
</style>
