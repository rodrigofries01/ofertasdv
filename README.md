# OfertasDV (Back-end)

API REST em Spring Boot para cadastro, aprovação e listagem de ofertas comerciais, com autenticação JWT, auditoria de ações e upload simples de imagens.

## Resumo do projeto
- Stack: Java 21, Spring Boot 3 (Web, Security, Validation, Data JPA), PostgreSQL, JJWT, Lombok.
- Módulos principais:
  - Autenticação JWT (`/api/auth`) com login e registro de usuários.
  - Ofertas (`/api/ofertas`): criar (multipart com imagem opcional), listar com paginação/filtro, aprovar/rejeitar.
  - Usuários (`/api/usuarios`): listagem e busca por id.
  - Auditoria (`/api/auditoria`): histórico de ações sobre ofertas.
- Upload local salvo em `uploads/` (uso apenas para desenvolvimento).

## Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL 14+ (local ou remoto)

## Configuração
Arquivo: `src/main/resources/application.properties`

```
# (Opcional) Tentar criar o banco se não existir (PostgreSQL)
app.db.create-if-missing=true

# Conexão da aplicação
spring.datasource.url=jdbc:postgresql://localhost:5432/ofertasdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Auto-criação de banco (opcional): para funcionar, informe também as credenciais administrativas via variáveis/propriedades:
- `app.db.admin.url` (ex.: `jdbc:postgresql://localhost:5432/postgres`)
- `app.db.admin.username` e `app.db.admin.password`

Notas de segurança/configuração:
- Troque a chave JWT em `JwtUtil.SECRET` por uma chave longa e segura (>= 32 chars) em produção.
- O caminho `/uploads/**` está liberado no `SecurityConfig`. Em produção, prefira um storage externo (S3, etc.) e regras mais restritivas.
- Atualmente todo `/api/ofertas/**` está `permitAll()` no `SecurityConfig`; ajuste para exigir autenticação/perfis quando necessário.

## Como executar
1. Configure o PostgreSQL e atualize `application.properties` conforme seu ambiente.
2. (Opcional) Defina as variáveis de auto-criação do banco, se desejar.
3. Rodar com Maven:
   - Linux/macOS: `./mvnw spring-boot:run`
   - Windows: `mvnw.cmd spring-boot:run`

Build de produção: `./mvnw clean package` (gerará JAR em `target/`).

Testes: `./mvnw test`

## Modelos principais
- `Usuario { id, nome, email, senha, papel=[ADMINISTRADOR|COMERCIANTE|USUARIO], dataCriacao }`
- `Oferta { id, nomeProduto, preco, quantidade, descricao, fotoUrl, status=[PENDENTE|APROVADO|REJEITADO], dataCriacao, administrador, comerciante }`
- `Auditoria { id, oferta, usuario, acao, dataAcao }`

## Endpoints (resumo)
Base URL: `http://localhost:8080`

Autenticação (`/api/auth`):
- `POST /api/auth/login` – body JSON `{ "email": "...", "senha": "..." }` → `AuthResponse { token }`
- `POST /api/auth/register` – body `UsuarioCreateDto` `{ nome, email, senha, papel }` → 201 ou 409 se e-mail existir

Usuários (`/api/usuarios`):
- `GET /api/usuarios` – lista `UsuarioDto`
- `GET /api/usuarios/{id}` – detalhe `UsuarioDto`

Ofertas (`/api/ofertas`):
- `POST /api/ofertas` – multipart/form-data
  - Parte `oferta` (JSON `OfertaCreateDto`): `{ nomeProduto, preco, quantidade, descricao, fotoUrl?, comercianteId }`
  - Parte `foto` (arquivo) opcional
  - Retorna `OfertaDto`
- `GET /api/ofertas?nome=xyz&page=0&size=10` – paginação e filtro por nome; retorna `Page<OfertaDto>`
- `POST /api/ofertas/{id}/aprovar?adminId=1` – aprova; retorna `OfertaDto`
- `POST /api/ofertas/{id}/rejeitar?adminId=1&motivo=...` – rejeita; retorna `OfertaDto`

Auditoria (`/api/auditoria`):
- `GET /api/auditoria` – lista `AuditoriaDto`
- `GET /api/auditoria/oferta/{id}` – por oferta

Cabeçalho de autenticação (quando exigido):
```
Authorization: Bearer <seu_token_jwt>
```

## Exemplos de uso (cURL)
Login:
```
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@ex.com","senha":"123456"}'
```
Criar oferta com imagem:
```
curl -X POST http://localhost:8080/api/ofertas \
  -H "Authorization: Bearer <TOKEN>" \
  -F "oferta={\"nomeProduto\":\"Arroz\",\"preco\":10.5,\"quantidade\":2,\"descricao\":\"Pacote 5kg\",\"comercianteId\":1};type=application/json" \
  -F "foto=@/caminho/para/imagem.jpg"
```

## Estrutura do código (pastas principais)
- `config/` – segurança (JWT, filter, SecurityConfig) e banco (auto-criação opcional)
- `controller/` – endpoints REST
- `dto/` – objetos de transferência
- `model/` – entidades JPA (Oferta, Usuario, Auditoria)
- `repository/` – interfaces Spring Data JPA
- `service/` – regras de negócio (criar/aprovar/rejeitar oferta, auditoria, etc.)

## Observações
- O upload salva arquivos em `uploads/` na raiz do projeto e define `fotoUrl` como `/uploads/<arquivo>`. Em produção, mapeie um servidor estático ou use storage dedicado.
- Considere endurecer as regras de autorização no `SecurityConfig` para operações administrativas.
- Se algo não subir, verifique o pacote da classe `OfertasdvApplication` e o package base do projeto.
