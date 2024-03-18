# Projeto: API com autenticação por cookies temporario

Projeto usado para estudos, sem integração de banco de dados.<p>
O projeto foi desenvolvido em Clojure e tem como objetivo explorar o uso de cookies temporários com um exemplo de login e senha de usuário.<p>
Em resumo, existem dois usuários cadastrados (mockados diretamente no código).

-   Usuário comum 
```
id: test  
password: 123
```
-   Usuário admin 
```
id: admin 
password: admin
```

Cadastro de usuário comum: depois de logado, tem acesso apenas às URIs permitidas.<p>
Cadastro de admin: depois de logado, tem acesso às URIs de usuário comum e também à área restrita para administradores ```/admin ```<p>
Após o login, o acesso é permitido por 120 segundos (controlado pelo cookie temporário), sendo necessário fazer login novamente após esse período.

## Rodando o projeto

Necessário ter o docker instalado.

Iniciar o terminal no diretório do projeto e digitar o comando

```
docker run --rm --name clojure -it -v %cd%:/work -w /work -p 3010:3010 clojure bash
```
Caso use o PowerShell do windonws substituir ```%cd%``` por ```${pwd}```

Em seguida usar o comando para subir a aplicação

```
lein run
```

Pronto, já podemos testar.

 [localhost:3010/health](http://localhost:3010/health)
 <p>Deve retornar um json com a msg de Ok, para garantir que o projeto está pronto pra uso.

 [localhost:3010/login](http://localhost:3010/login)
 <p>Mostra na tela dois inputs para inserir Id e Senha (já mencionados).

 [localhost:3010/api/v1](http://localhost:3010/api/v1)
 <p>Deve retornar uma mensagem de OK se login e senha for compativel.

 [localhost:3010/api/v1/admin](http://localhost:3010/api/v1/admin)
 <p>Deve retornar uma mensagem de OK apenas quando logado como admin.

 <u>Pode ser testado outros methodos além do GET também através do Postman. 
