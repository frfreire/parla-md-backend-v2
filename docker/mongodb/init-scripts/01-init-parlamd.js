// =========================================================================
// PARLA-MD - SCRIPT DE INICIALIZAÇÃO DO MONGODB
// =========================================================================
// Este script configura o banco de dados e usuário para a aplicação Parla-MD
// Executado automaticamente durante a criação do container MongoDB
// =========================================================================

// Conectar ao banco de administração
print('🔄 Iniciando configuração do MongoDB para Parla-MD...');

// Autenticar como admin
db = db.getSiblingDB('admin');
db.auth(process.env.MONGO_INITDB_ROOT_USERNAME, process.env.MONGO_INITDB_ROOT_PASSWORD);

// Criar banco de dados da aplicação
db = db.getSiblingDB('parlamd');

// Criar usuário específico para a aplicação (se não existir)
try {
    db.createUser({
        user: "parlamd_user",
        pwd: "parlamd123",
        roles: [
            {
                role: "readWrite",
                db: "parlamd"
            }
        ]
    });
    print('✅ Usuário parlamd_user criado com sucesso');
} catch (e) {
    if (e.code === 11000) {
        print('ℹ️  Usuário parlamd_user já existe');
    } else {
        print('⚠️  Erro ao criar usuário: ' + e.message);
    }
}

// Criar collections básicas com índices
try {
    // Collection de proposições
    db.createCollection("proposicoes");
    db.proposicoes.createIndex({ "numero": 1, "ano": 1, "tipo": 1 }, { unique: true });
    db.proposicoes.createIndex({ "dataApresentacao": -1 });
    db.proposicoes.createIndex({ "status": 1 });
    db.proposicoes.createIndex({ "autor": 1 });
    
    // Collection de deputados
    db.createCollection("deputados");
    db.deputados.createIndex({ "id": 1 }, { unique: true });
    db.deputados.createIndex({ "nome": 1 });
    db.deputados.createIndex({ "partido": 1 });
    db.deputados.createIndex({ "uf": 1 });
    
    // Collection de senadores
    db.createCollection("senadores");
    db.senadores.createIndex({ "id": 1 }, { unique: true });
    db.senadores.createIndex({ "nome": 1 });
    db.senadores.createIndex({ "partido": 1 });
    db.senadores.createIndex({ "uf": 1 });
    
    // Collection de análises
    db.createCollection("analises");
    db.analises.createIndex({ "proposicaoId": 1 });
    db.analises.createIndex({ "dataAnalise": -1 });
    db.analises.createIndex({ "tipoAnalise": 1 });
    
    print('✅ Collections e índices criados com sucesso');
} catch (e) {
    print('⚠️  Erro ao criar collections: ' + e.message);
}

// Inserir dados de configuração inicial
try {
    db.configuracoes.insertOne({
        chave: "versao_sistema",
        valor: "1.0.0",
        descricao: "Versão atual do sistema Parla-MD",
        dataAtualizacao: new Date()
    });
    
    db.configuracoes.insertOne({
        chave: "ultima_sincronizacao_camara",
        valor: null,
        descricao: "Data da última sincronização com API da Câmara",
        dataAtualizacao: new Date()
    });
    
    db.configuracoes.insertOne({
        chave: "ultima_sincronizacao_senado",
        valor: null,
        descricao: "Data da última sincronização com API do Senado",
        dataAtualizacao: new Date()
    });
    
    print('✅ Configurações iniciais inseridas');
} catch (e) {
    print('⚠️  Erro ao inserir configurações: ' + e.message);
}

print('🎉 Inicialização do MongoDB concluída com sucesso!');
