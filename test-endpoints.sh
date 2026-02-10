#!/bin/bash
# =====================================================
# Teste dos Endpoints REST da Multitenant Application
# =====================================================
# Este script demonstra como testar os endpoints da aplicação
# usando curl. Certifique-se de que a aplicação está rodando
# em http://localhost:8080 antes de executar este script.

API_URL="http://localhost:8080/api/clientes"
TENANT_ID="tenant-001"

echo "=================================================="
echo "Testes dos Endpoints REST - Multitenant App"
echo "=================================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# =====================================================
# Teste 1: Health Check
# =====================================================
echo -e "${BLUE}[TESTE 1] Health Check${NC}"
echo "Endpoint: GET $API_URL/health"
echo "Descrição: Verifica se a aplicação está rodando"
echo ""
curl -X GET "$API_URL/health" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 1 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 2: Listar Todos os Clientes
# =====================================================
echo -e "${BLUE}[TESTE 2] Listar Todos os Clientes${NC}"
echo "Endpoint: GET $API_URL/{tenantId}"
echo "Tenant ID: $TENANT_ID"
echo "Descrição: Busca todos os clientes do tenant especificado"
echo ""
curl -X GET "$API_URL/$TENANT_ID" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 2 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 3: Buscar Cliente Específico (ID = 1)
# =====================================================
echo -e "${BLUE}[TESTE 3] Buscar Cliente Específico (ID = 1)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}/{id}"
echo "Tenant ID: $TENANT_ID"
echo "Cliente ID: 1"
echo "Descrição: Busca um cliente específico pelo ID"
echo ""
curl -X GET "$API_URL/$TENANT_ID/1" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 3 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 4: Buscar Cliente Específico (ID = 2)
# =====================================================
echo -e "${BLUE}[TESTE 4] Buscar Cliente Específico (ID = 2)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}/{id}"
echo "Tenant ID: $TENANT_ID"
echo "Cliente ID: 2"
echo "Descrição: Busca um cliente específico pelo ID"
echo ""
curl -X GET "$API_URL/$TENANT_ID/2" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 4 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 5: Buscar Cliente Inexistente
# =====================================================
echo -e "${BLUE}[TESTE 5] Buscar Cliente Inexistente (ID = 999)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}/{id}"
echo "Tenant ID: $TENANT_ID"
echo "Cliente ID: 999"
echo "Descrição: Tenta buscar um cliente que não existe"
echo "Esperado: HTTP 404 Not Found"
echo ""
curl -X GET "$API_URL/$TENANT_ID/999" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 5 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 6: Tenant Diferente
# =====================================================
echo -e "${BLUE}[TESTE 6] Listar Clientes de Outro Tenant${NC}"
echo "Endpoint: GET $API_URL/{tenantId}"
echo "Tenant ID: tenant-002"
echo "Descrição: Busca clientes de um tenant diferente"
echo ""
curl -X GET "$API_URL/tenant-002" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 6 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 7: Tenant Inválido (vazio)
# =====================================================
echo -e "${BLUE}[TESTE 7] Tenant Inválido (vazio)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}"
echo "Tenant ID: (vazio)"
echo "Descrição: Tenta acessar com tenant vazio"
echo "Esperado: HTTP 400 Bad Request"
echo ""
curl -X GET "$API_URL//" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 7 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 8: ID Inválido (negativo)
# =====================================================
echo -e "${BLUE}[TESTE 8] ID de Cliente Inválido (negativo)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}/{id}"
echo "Tenant ID: $TENANT_ID"
echo "Cliente ID: -1"
echo "Descrição: Tenta buscar cliente com ID negativo"
echo "Esperado: HTTP 400 Bad Request"
echo ""
curl -X GET "$API_URL/$TENANT_ID/-1" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 8 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Teste 9: ID Inválido (não numérico)
# =====================================================
echo -e "${BLUE}[TESTE 9] ID de Cliente Inválido (não numérico)${NC}"
echo "Endpoint: GET $API_URL/{tenantId}/{id}"
echo "Tenant ID: $TENANT_ID"
echo "Cliente ID: abc"
echo "Descrição: Tenta buscar cliente com ID não numérico"
echo "Esperado: HTTP 400 Bad Request"
echo ""
curl -X GET "$API_URL/$TENANT_ID/abc" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq .
echo ""
echo -e "${GREEN}✓ Teste 9 Concluído${NC}"
echo ""
echo "=================================================="
echo ""

# =====================================================
# Resumo
# =====================================================
echo -e "${YELLOW}RESUMO DOS TESTES${NC}"
echo ""
echo "Total de testes executados: 9"
echo ""
echo "Testes realizados:"
echo "  1. Health Check - Status da aplicação"
echo "  2. Listar todos os clientes - Sucesso esperado"
echo "  3. Buscar cliente ID 1 - Sucesso esperado"
echo "  4. Buscar cliente ID 2 - Sucesso esperado"
echo "  5. Buscar cliente ID 999 - 404 Not Found esperado"
echo "  6. Buscar clientes de outro tenant - Resultado depende da configuração"
echo "  7. Tenant inválido (vazio) - 400 Bad Request esperado"
echo "  8. ID negativo - 400 Bad Request esperado"
echo "  9. ID não numérico - 400 Bad Request esperado"
echo ""
echo "=================================================="
echo ""
echo -e "${GREEN}Testes concluídos!${NC}"
echo ""
echo "Dicas:"
echo "  • Certifique-se de que a aplicação está rodando: mvn spring-boot:run"
echo "  • Certifique-se de que PostgreSQL está rodando e o banco foi criado"
echo "  • Use 'jq' para formatar o JSON (instalação: brew install jq)"
echo "  • Para ver detalhes adicionais, remova ' | jq .' do comando curl"
echo ""
