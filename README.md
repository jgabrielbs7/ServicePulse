# Help Desk – Gestão de Chamados

Spring Boot 3 · PostgreSQL · Thymeleaf · Tailwind CSS · Chart.js

## Como rodar
1. Suba o PostgreSQL: `docker compose up -d` (ou crie manualmente o banco `helpdesk`).
2. Execute: `mvn spring-boot:run`
3. Acesse http://localhost:8080

Requisitos: JDK 17+, Maven 3.9+ e internet (Tailwind e Chart.js são carregados por CDN).
Para outro banco, use as variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Primeiro acesso
Por padrão **não há usuários nem senhas padrão**. Com o banco vazio, o sistema cria o primeiro
administrador a partir de variáveis de ambiente (senha com no mínimo 12 caracteres):

    BOOTSTRAP_ADMIN_NAME=Administrador
    BOOTSTRAP_ADMIN_EMAIL=voce@empresa.com
    BOOTSTRAP_ADMIN_PASSWORD=uma-senha-forte-aqui

**Somente para testes locais**, com banco vazio, `SEED_DEMO_DATA=true` cria dados de exemplo
(admin@helpdesk.com / admin123, tecnico@helpdesk.com / tecnico123 e chamados fictícios).
Nunca use isso em produção.

## Variáveis de ambiente
| Variável | Descrição | Padrão |
|---|---|---|
| `DB_URL` | URL JDBC (`jdbc:postgresql://host/banco?sslmode=require`) | localhost |
| `DB_USER` / `DB_PASSWORD` | Credenciais do banco | postgres |
| `PORT` | Porta HTTP | 8080 |
| `SEED_DEMO_DATA` | Dados de exemplo | false |
| `BOOTSTRAP_ADMIN_*` | Primeiro administrador | vazio |

## Como funciona
Clientes não acessam o sistema: são apenas cadastros. O técnico abre o chamado em nome do cliente.

| Área                      | ADMIN | TECNICO |
|---------------------------|:-----:|:-------:|
| Chamados (dashboard)      |  ✔    |   ✔     |
| Abrir/editar chamados     |  ✔    |   ✔     |
| Clientes (cadastro)       |  ✔    |   ✔     |
| Analytics                 |  ✔    |         |
| Usuários e permissões     |  ✔    |         |
| Motivos de chamado        |  ✔    |         |

Regras da gestão de usuários: não é possível desativar/rebaixar a si mesmo nem o último administrador ativo.
Usuários são desativados (não excluídos) para preservar o histórico dos chamados.

## Publicar gratuitamente no Render

O projeto inclui um `Dockerfile` para o Render executar a aplicação Spring Boot com Java 17.

1. Envie este projeto para um repositório no GitHub.
2. No Render, escolha **New + → PostgreSQL** e crie um banco. Copie o hostname interno, nome do banco, usuário e senha mostrados nos detalhes do banco.
3. No Render, escolha **New + → Web Service**, conecte o repositório e selecione o runtime **Docker**. Escolha o plano **Free**.
4. Em **Environment Variables**, adicione:
   - `DB_URL` = `jdbc:postgresql://HOST_INTERNO:5432/NOME_DO_BANCO`
   - `DB_USER` = usuário do banco
   - `DB_PASSWORD` = senha do banco
   - `BOOTSTRAP_ADMIN_NAME` = nome do administrador inicial
   - `BOOTSTRAP_ADMIN_EMAIL` = seu e-mail de acesso
   - `BOOTSTRAP_ADMIN_PASSWORD` = senha forte com pelo menos 12 caracteres
   - `SEED_DEMO_DATA` = `false`
5. Clique em **Create Web Service** e acompanhe o build em **Logs**. Quando o deploy terminar, o Render mostrará o endereço público `*.onrender.com`.
6. Entre com o e-mail e a senha que configurou nas variáveis de ambiente. Não compartilhe essas credenciais.

### Atenção sobre os planos gratuitos

- O serviço web gratuito do Render entra em suspensão após 15 minutos sem tráfego e pode levar cerca de um minuto para voltar a responder.
- O PostgreSQL gratuito do Render expira após 30 dias. É adequado para uma demonstração temporária, mas não para guardar dados importantes por longo prazo. Faça uma cópia dos dados antes da expiração ou use um banco com uma política de retenção compatível.
- Não use dados reais de clientes. Para portfólio, prefira dados fictícios.
- Nunca coloque senhas no código nem no repositório; configure-as somente em **Environment Variables** no Render.

Documentação oficial: https://render.com/docs/docker, https://render.com/docs/databases e https://render.com/docs/free.
