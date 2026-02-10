#!/bin/bash
# =====================================================
# Docker Helper Script para PostgreSQL
# Facilita operações comuns com Docker e PostgreSQL
# =====================================================

set -e

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Variáveis
COMPOSE_FILE="docker-compose.yml"
CONTAINER_NAME="multitenant-postgres-db"
IMAGE_NAME="multitenant-postgres:latest"

# =====================================================
# Funções de Utilidade
# =====================================================

print_header() {
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# =====================================================
# Comando: Iniciar Docker Compose
# =====================================================

cmd_start() {
    print_header "Iniciando PostgreSQL com Docker Compose"
    
    if [ ! -f "$COMPOSE_FILE" ]; then
        print_error "Arquivo $COMPOSE_FILE não encontrado"
        exit 1
    fi
    
    docker-compose up -d
    print_success "PostgreSQL iniciado com sucesso"
    print_warning "Aguarde alguns segundos para o banco estar pronto..."
    sleep 5
    docker-compose ps
}

# =====================================================
# Comando: Parar Docker Compose
# =====================================================

cmd_stop() {
    print_header "Parando PostgreSQL"
    docker-compose stop
    print_success "PostgreSQL parado (dados preservados)"
}

# =====================================================
# Comando: Remover containers e volumes
# =====================================================

cmd_down() {
    print_header "Removendo containers e volumes"
    print_warning "Isso vai APAGAR todos os dados do banco!"
    read -p "Tem certeza? (s/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        docker-compose down -v
        print_success "Containers e volumes removidos"
    else
        print_warning "Operação cancelada"
    fi
}

# =====================================================
# Comando: Ver status
# =====================================================

cmd_status() {
    print_header "Status dos Containers"
    docker-compose ps
}

# =====================================================
# Comando: Ver logs
# =====================================================

cmd_logs() {
    print_header "Logs do PostgreSQL"
    if [ "$1" == "-f" ]; then
        docker-compose logs -f postgres
    else
        docker-compose logs postgres | tail -50
    fi
}

# =====================================================
# Comando: Conectar ao banco com psql
# =====================================================

cmd_connect() {
    print_header "Conectando ao PostgreSQL"
    
    if ! command -v psql &> /dev/null; then
        print_error "psql não está instalado"
        echo "Use: docker-compose exec postgres psql -U postgres -d multitenant_db"
        exit 1
    fi
    
    psql -h localhost -U postgres -d multitenant_db
}

# =====================================================
# Comando: Conectar via Docker exec
# =====================================================

cmd_exec() {
    print_header "Conectando ao PostgreSQL (via Docker)"
    docker-compose exec postgres psql -U postgres -d multitenant_db
}

# =====================================================
# Comando: Listar bancos de dados
# =====================================================

cmd_list_databases() {
    print_header "Bancos de Dados Disponíveis"
    docker-compose exec postgres psql -U postgres -l
}

# =====================================================
# Comando: Listar tabelas
# =====================================================

cmd_list_tables() {
    print_header "Tabelas do banco multitenant_db"
    docker-compose exec postgres psql -U postgres -d multitenant_db -c "\dt"
}

# =====================================================
# Comando: Contar registros
# =====================================================

cmd_count_records() {
    print_header "Contando Registros na Tabela 'clientes'"
    docker-compose exec postgres psql -U postgres -d multitenant_db -c "SELECT COUNT(*) as total_clientes FROM clientes;"
}

# =====================================================
# Comando: Ver todos os clientes
# =====================================================

cmd_view_clients() {
    print_header "Listando Todos os Clientes"
    docker-compose exec postgres psql -U postgres -d multitenant_db -c "SELECT id, nome, email FROM clientes ORDER BY id;"
}

# =====================================================
# Comando: Fazer backup
# =====================================================

cmd_backup() {
    print_header "Fazendo Backup do Banco"
    
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    docker-compose exec postgres pg_dump -U postgres -d multitenant_db > "$BACKUP_FILE"
    
    print_success "Backup criado: $BACKUP_FILE"
    ls -lh "$BACKUP_FILE"
}

# =====================================================
# Comando: Restaurar backup
# =====================================================

cmd_restore() {
    print_header "Restaurando Backup"
    
    if [ -z "$1" ]; then
        print_error "Uso: $0 restore <arquivo_backup>"
        echo "Exemplos de backup:"
        ls -1 backup_*.sql 2>/dev/null || echo "Nenhum backup encontrado"
        exit 1
    fi
    
    if [ ! -f "$1" ]; then
        print_error "Arquivo não encontrado: $1"
        exit 1
    fi
    
    print_warning "Isso vai sobrescrever o banco atual!"
    read -p "Tem certeza? (s/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        docker-compose exec -T postgres psql -U postgres -d multitenant_db < "$1"
        print_success "Banco restaurado de: $1"
    else
        print_warning "Operação cancelada"
    fi
}

# =====================================================
# Comando: Health check
# =====================================================

cmd_health() {
    print_header "Verificando Saúde do PostgreSQL"
    
    if docker-compose exec postgres pg_isready -U postgres > /dev/null 2>&1; then
        print_success "PostgreSQL está saudável e pronto para conexões"
    else
        print_error "PostgreSQL não está respondendo"
    fi
}

# =====================================================
# Comando: Build da imagem
# =====================================================

cmd_build() {
    print_header "Compilando Imagem Docker"
    docker build -t "$IMAGE_NAME" .
    print_success "Imagem compilada: $IMAGE_NAME"
}

# =====================================================
# Comando: Rebuild completo
# =====================================================

cmd_rebuild() {
    print_header "Rebuilding Docker Compose"
    docker-compose up -d --build
    print_success "Docker Compose rebuilded com sucesso"
    sleep 5
    docker-compose ps
}

# =====================================================
# Comando: Limpar (remove dangling images)
# =====================================================

cmd_clean() {
    print_header "Limpando Docker (dangling images)"
    docker image prune -f
    docker volume prune -f
    print_success "Docker limpado"
}

# =====================================================
# Comando: Ver ajuda
# =====================================================

cmd_help() {
    cat << EOF
${BLUE}PostgreSQL Docker Helper Script${NC}

${YELLOW}Uso:${NC}
  $0 <comando> [opcões]

${YELLOW}Comandos:${NC}

  ${BLUE}Operações Básicas:${NC}
    start              Iniciar PostgreSQL com Docker Compose
    stop               Parar PostgreSQL (dados preservados)
    down               Remover containers e volumes (APAGA DADOS)
    status             Ver status dos containers
    rebuild            Rebuild Docker Compose
    clean              Limpar images e volumes não utilizados

  ${BLUE}Conexão e Consultas:${NC}
    connect            Conectar ao banco com psql (se instalado)
    exec               Conectar ao banco via docker exec
    health             Verificar saúde do PostgreSQL
    
  ${BLUE}Gerenciamento de Banco:${NC}
    databases          Listar bancos de dados
    tables             Listar tabelas
    count              Contar registros na tabela clientes
    clients            Ver todos os clientes
    logs               Ver logs (use -f para tempo real)
    
  ${BLUE}Backup e Restore:${NC}
    backup             Fazer backup do banco
    restore <arquivo>  Restaurar backup

  ${BLUE}Desenvolvimento:${NC}
    build              Compilar imagem Docker
    help               Mostrar esta ajuda

${YELLOW}Exemplos:${NC}
  $0 start                        # Iniciar PostgreSQL
  $0 status                       # Ver status
  $0 logs -f                      # Ver logs em tempo real
  $0 connect                      # Conectar ao banco
  $0 backup                       # Fazer backup
  $0 restore backup_20260209.sql  # Restaurar backup

${YELLOW}Pré-requisitos:${NC}
  - Docker Desktop
  - Docker Compose

${YELLOW}Credenciais Padrão:${NC}
  Usuário: postgres
  Senha: postgres
  Porta: 5432

EOF
}

# =====================================================
# Main - Processar argumentos
# =====================================================

main() {
    if [ $# -eq 0 ]; then
        cmd_help
        exit 0
    fi
    
    COMMAND="$1"
    shift
    
    case "$COMMAND" in
        start)      cmd_start ;;
        stop)       cmd_stop ;;
        down)       cmd_down ;;
        status)     cmd_status ;;
        logs)       cmd_logs "$@" ;;
        connect)    cmd_connect ;;
        exec)       cmd_exec ;;
        databases)  cmd_list_databases ;;
        tables)     cmd_list_tables ;;
        count)      cmd_count_records ;;
        clients)    cmd_view_clients ;;
        health)     cmd_health ;;
        backup)     cmd_backup ;;
        restore)    cmd_restore "$@" ;;
        build)      cmd_build ;;
        rebuild)    cmd_rebuild ;;
        clean)      cmd_clean ;;
        help)       cmd_help ;;
        *)
            print_error "Comando desconhecido: $COMMAND"
            echo ""
            cmd_help
            exit 1
            ;;
    esac
}

# Executar main
main "$@"
