# 🎬 CineFlix

Aplicativo Android para pesquisa, visualização e organização de filmes, desenvolvido em **Kotlin** no **Android Studio**.

O projeto foi desenvolvido como parte das atividades acadêmicas do **Módulo 5**, com foco na aplicação de diferentes formas de persistência de dados em aplicativos Android.

---

## 👤 Autor

**Alex Magalhães Santos**

---

## 📖 Sobre o projeto

O **CineFlix** é um aplicativo de filmes inspirado visualmente em plataformas de streaming.

O projeto teve início no MIT App Inventor e, posteriormente, foi recriado no Android Studio para permitir a implementação de recursos mais avançados, como:

- Pesquisa de filmes pela internet;
- Catalogo local em JSON;
- Banco de dados SQLite;
- Histórico de pesquisas;
- Preferências do usuário;
- Múltiplas Activities;
- Navegação por Intents;
- Leitura e gravação de arquivos;
- Exportação de dados;
- Tratamento de erros e ausência de conexão.

O aplicativo permite pesquisar filmes, visualizar informações detalhadas, adicionar títulos aos favoritos, recuperar a última pesquisa, consultar o histórico e acessar configurações.

---

## 🎯 Objetivos do projeto

O principal objetivo do CineFlix é aplicar, de forma prática, os conceitos estudados durante o desenvolvimento de aplicativos Android, especialmente:

- Persistência de dados com `SharedPreferences`;
- Compartilhamento das mesmas preferências entre Activities;
- Salvamento e leitura de arquivos internos;
- Salvamento de arquivos no armazenamento externo;
- Leitura de arquivos da pasta `res/raw`;
- Criação e utilização de banco de dados SQLite;
- Navegação entre diferentes Activities;
- Compartilhamento de informações com `Intent`;
- Consumo de uma API externa;
- Utilização de `RecyclerView`;
- Organização do código em classes com responsabilidades específicas.

---

# 📱 Funcionalidades

## 🔎 Pesquisa de filmes

O CineFlix combina uma pesquisa local com uma consulta on-line à API do **The Movie Database — TMDB**.

Enquanto o usuário digita, o aplicativo utiliza um `TextWatcher` para filtrar os filmes que já estão carregados no catálogo local.

A pesquisa on-line é realizada quando o usuário:

- Toca no botão ou ícone de pesquisa;
- Seleciona a ação de pesquisa no teclado virtual;
- Seleciona a ação de conclusão do teclado;
- Pressiona a tecla Enter.

Antes de executar a consulta on-line, o termo pesquisado é salvo no histórico interno do aplicativo.

---

## 🔤 Pesquisa por título e gênero

A pesquisa local pode localizar filmes por:

- Título;
- Gênero.

A filtragem é aplicada ao catálogo local enquanto o usuário digita.

Caso nenhum resultado seja encontrado localmente, o aplicativo orienta o usuário a tocar na lupa para realizar uma pesquisa on-line no TMDB.

---

## 🔡 Pesquisa com ou sem acentos

O CineFlix normaliza os textos antes de realizar a comparação.

A normalização:

- Remove acentos;
- Converte o texto para letras minúsculas;
- Remove espaços extras.

Com isso, pesquisas como:

```text
Ação
acao
AÇÃO
Acao
```

podem retornar os mesmos resultados.

A normalização é realizada utilizando a classe `Normalizer`:

```kotlin
private fun normalizarTexto(texto: String): String {
    return Normalizer.normalize(
        texto,
        Normalizer.Form.NFD
    )
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .trim()
}
```

---

## 🌐 Pesquisa on-line no TMDB

A comunicação com a API do TMDB é realizada pela classe:

```text
TmdbRepository
```

A pesquisa é executada em uma thread separada para evitar que a interface do aplicativo fique bloqueada durante a consulta.

Enquanto os dados são carregados, o aplicativo apresenta a mensagem:

```text
Buscando filmes...
```

Os resultados recebidos são apresentados no `RecyclerView` por meio do `MovieAdapter`.

---

## 🔄 Proteção contra resultados antigos

Antes de atualizar a interface com os resultados do TMDB, o aplicativo verifica se o usuário ainda está pesquisando o mesmo termo.

```kotlin
val textoAtual = binding.editSearch.text
    .toString()
    .trim()

if (textoAtual != termo) {
    return@runOnUiThread
}
```

Essa verificação evita que o resultado de uma pesquisa antiga substitua os dados de uma pesquisa mais recente.

O aplicativo também verifica se a Activity ainda está ativa:

```kotlin
if (isFinishing || isDestroyed) {
    return@runOnUiThread
}
```

---

## 📵 Tratamento de erros e funcionamento sem conexão

Caso ocorra uma falha ao consultar o TMDB, o erro é registrado no Logcat:

```kotlin
Log.e(
    "CineFlixTMDB",
    "Erro ao consultar o TMDB",
    e
)
```

Depois disso, o aplicativo utiliza o catálogo local como alternativa.

O CineFlix:

1. Recupera os filmes do catálogo local;
2. Filtra os filmes de acordo com o termo digitado;
3. Atualiza o `MovieAdapter`;
4. Informa ao usuário que a consulta on-line não foi concluída.

Exemplo de mensagem:

```text
Não foi possível consultar o TMDB. Verifique sua conexão...
```

---

## 🎞️ Catálogo local

O aplicativo possui um catálogo local armazenado no arquivo:

```text
movies.json
```

O arquivo está localizado em:

```text
app/src/main/res/raw/movies.json
```

Sua leitura é realizada por meio do método:

```kotlin
resources.openRawResource(R.raw.movies)
```

A classe `MovieRepository` é responsável por carregar e converter os dados do arquivo JSON em objetos do tipo `Movie`.

---

## 📄 Detalhes do filme

Ao selecionar um filme, o usuário é direcionado para a `DetalhesActivity`.

A `MainActivity` envia o identificador do filme utilizando uma `Intent`:

```kotlin
val intent = Intent(
    this,
    DetalhesActivity::class.java
)

intent.putExtra("movie_id", filme.id)
startActivity(intent)
```

Os filmes encontrados pela internet são registrados temporariamente no `MovieRepository`:

```kotlin
MovieRepository.registrarFilmesOnline(
    filmesEncontrados
)
```

Isso permite que a tela de detalhes localize o filme selecionado por seu identificador.

---

## ❤️ Filmes favoritos

O usuário pode adicionar filmes à lista de favoritos.

Os filmes favoritos são armazenados permanentemente em um banco de dados SQLite e continuam disponíveis mesmo depois que o aplicativo é fechado.

Entre as operações disponíveis estão:

- Adicionar um filme aos favoritos;
- Consultar os filmes salvos;
- Verificar se um filme já está salvo;
- Remover um filme;
- Exibir a lista de favoritos;
- Exportar os dados dos favoritos.

A tela responsável pela exibição dos filmes salvos é:

```text
FavoritosActivity
```

---

## 🕘 Histórico de pesquisas

As pesquisas realizadas são salvas no armazenamento interno do aplicativo.

A classe responsável por esse processo é:

```text
HistoricoManager
```

Antes de executar a pesquisa on-line, a `MainActivity` chama:

```kotlin
HistoricoManager.salvar(
    this,
    termo
)
```

Os dados são gravados em um arquivo interno, permitindo que o histórico seja recuperado posteriormente.

---

## 💾 Última pesquisa

A última pesquisa digitada é armazenada por meio da classe:

```text
PreferencesManager
```

Durante a digitação, o valor é salvo:

```kotlin
PreferencesManager.setUltimaPesquisa(
    this@MainActivity,
    termo
)
```

Quando o aplicativo é aberto novamente, a pesquisa é recuperada:

```kotlin
val ultimaPesquisa =
    PreferencesManager.getUltimaPesquisa(this)
```

O texto salvo é inserido novamente no campo de pesquisa e uma prévia dos resultados locais é apresentada.

---

## ⚙️ Configurações

A tela de configurações é representada pela:

```text
ConfiguracoesActivity
```

Ela é acessada pelo menu da tela principal e reúne as preferências do aplicativo.

As configurações simples podem ser compartilhadas entre diferentes Activities utilizando o mesmo arquivo de `SharedPreferences`.

---

## 📤 Exportação de dados

A lista de favoritos pode ser exportada para um arquivo no formato CSV.

Exemplo de nome do arquivo:

```text
favoritos_cineflix.csv
```

O arquivo pode conter informações como:

- Título;
- Gêneros;
- Ano;
- Nota;
- Sinopse.

A exportação utiliza o armazenamento externo do aplicativo.

---

# 💾 Persistência de dados

O principal foco do Módulo 5 foi implementar diferentes formas de armazenamento e recuperação de dados.

---

## 1. SharedPreferences

O CineFlix utiliza `SharedPreferences` para armazenar dados simples do usuário.

Entre os dados salvos estão:

- Última pesquisa digitada;
- Preferências definidas na tela de configurações;
- Outras informações simples que precisam permanecer disponíveis depois que o aplicativo é fechado.

As operações são centralizadas pela classe:

```text
PreferencesManager
```

---

## 2. Compartilhamento de preferências entre Activities

As diferentes Activities podem acessar o mesmo arquivo de preferências utilizando:

```kotlin
getSharedPreferences()
```

Dessa maneira, uma configuração salva em uma tela pode ser recuperada por outra Activity.

Isso mantém as preferências consistentes durante a navegação pelo aplicativo.

---

## 3. Armazenamento interno

O histórico de pesquisas é salvo no armazenamento interno do aplicativo.

A gravação utiliza `FileOutputStream`, enquanto a leitura pode utilizar:

- `FileInputStream`;
- `InputStreamReader`;
- `BufferedReader`.

Esses arquivos ficam em uma área privada do aplicativo.

---

## 4. Armazenamento externo

A exportação dos favoritos utiliza o armazenamento externo para gerar um arquivo CSV.

Nas versões atuais do Android, pode ser utilizado o diretório externo específico do aplicativo:

```kotlin
getExternalFilesDir(
    Environment.DIRECTORY_DOCUMENTS
)
```

Esse local oferece maior compatibilidade com as regras atuais de armazenamento do Android.

---

## 5. Arquivo da pasta res/raw

O arquivo `movies.json` está incluído diretamente nos recursos do projeto.

Seu conteúdo é acessado por meio de:

```kotlin
resources.openRawResource(R.raw.movies)
```

A leitura é realizada pela classe `MovieRepository`.

---

## 6. Banco de dados SQLite

Os filmes favoritos são armazenados em um banco SQLite.

A classe auxiliar responsável pelo banco é:

```text
DatabaseHelper
```

Ela estende:

```kotlin
SQLiteOpenHelper
```

O banco utilizado pelo aplicativo é:

```text
cineflix.db
```

A tabela principal é:

```text
favoritos
```

Entre os dados que podem ser armazenados estão:

- Identificador do filme;
- Título;
- Gêneros;
- Ano;
- Nota;
- Sinopse;
- Endereço do pôster.

A classe `DatabaseHelper` é responsável por:

- Criar o banco de dados;
- Criar a tabela de favoritos;
- Adicionar filmes;
- Consultar filmes salvos;
- Verificar se um filme já está nos favoritos;
- Remover filmes;
- Retornar a lista completa de favoritos.

---

# 🧭 Navegação entre telas

O CineFlix utiliza múltiplas Activities.

## MainActivity

Tela principal do aplicativo.

Responsável por:

- Carregar o catálogo local;
- Exibir os filmes;
- Realizar a pesquisa local;
- Realizar a pesquisa no TMDB;
- Salvar a última pesquisa;
- Salvar o histórico;
- Abrir a tela de detalhes;
- Abrir o menu principal.

## DetalhesActivity

Responsável por apresentar os dados completos do filme selecionado.

O filme é localizado por meio do valor:

```text
movie_id
```

recebido pela `Intent`.

## FavoritosActivity

Responsável por apresentar os filmes armazenados no banco de dados SQLite.

## ConfiguracoesActivity

Responsável pelas preferências e configurações do aplicativo.

---

## Navegação pelo menu

A `MainActivity` possui um menu de opções definido no arquivo:

```text
app/src/main/res/menu/menu_main.xml
```

O menu é carregado por:

```kotlin
override fun onCreateOptionsMenu(
    menu: Menu
): Boolean {
    menuInflater.inflate(
        R.menu.menu_main,
        menu
    )

    return true
}
```

As opções são tratadas em:

```kotlin
onOptionsItemSelected()
```

O menu possui opções para abrir:

- `FavoritosActivity`;
- `ConfiguracoesActivity`.

A navegação é realizada com `Intent`.

---

# 🧱 Principais classes

## MainActivity

Controla a tela inicial, o catálogo, a pesquisa local e a pesquisa on-line.

## DetalhesActivity

Apresenta os dados completos do filme selecionado.

## FavoritosActivity

Exibe os filmes salvos no SQLite.

## ConfiguracoesActivity

Gerencia a tela de configurações.

## Movie

Representa os dados de um filme.

## MovieAdapter

Atualiza e apresenta os filmes no `RecyclerView`.

## MovieRepository

Responsável por:

- Ler o arquivo `movies.json`;
- Manter o catálogo local;
- Registrar temporariamente os filmes encontrados on-line;
- Localizar filmes por identificador.

## TmdbRepository

Responsável pela comunicação com a API do TMDB.

## PreferencesManager

Responsável pelo salvamento e recuperação de dados simples com `SharedPreferences`.

## HistoricoManager

Responsável pelo salvamento e leitura do histórico de pesquisas em arquivo interno.

## DatabaseHelper

Responsável pela criação e pelo gerenciamento do banco SQLite.

---

# 🛠️ Tecnologias utilizadas

- Android Studio;
- Kotlin;
- XML;
- Android SDK;
- View Binding;
- RecyclerView;
- LinearLayoutManager;
- TextWatcher;
- Intent;
- SharedPreferences;
- FileOutputStream;
- FileInputStream;
- InputStreamReader;
- BufferedReader;
- SQLite;
- SQLiteOpenHelper;
- JSON;
- API REST;
- TMDB API;
- Threads;
- Git;
- GitHub.

---

# 🔐 Configuração da API TMDB

A chave da API é acessada pelo aplicativo por meio do `BuildConfig`:

```kotlin
val apiKey =
    BuildConfig.TMDB_API_KEY.trim()
```

A chave não deve ser escrita diretamente na `MainActivity` nem publicada no GitHub.

Ela deve ser configurada localmente no arquivo:

```text
local.properties
```

Exemplo:

```properties
TMDB_API_KEY=SUA_CHAVE_DA_API
```

O arquivo `local.properties` deve permanecer incluído no `.gitignore`.

Caso a chave não esteja configurada, o CineFlix apresenta a mensagem:

```text
A chave da API TMDB não foi configurada.
```

---

# 📂 Estrutura resumida do projeto

```text
CineFlix/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/cineflix/
│   │       │       ├── MainActivity.kt
│   │       │       ├── DetalhesActivity.kt
│   │       │       ├── FavoritosActivity.kt
│   │       │       ├── ConfiguracoesActivity.kt
│   │       │       ├── Movie.kt
│   │       │       ├── MovieAdapter.kt
│   │       │       ├── MovieRepository.kt
│   │       │       ├── TmdbRepository.kt
│   │       │       ├── PreferencesManager.kt
│   │       │       ├── HistoricoManager.kt
│   │       │       └── DatabaseHelper.kt
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   ├── layout/
│   │       │   ├── menu/
│   │       │   │   └── menu_main.xml
│   │       │   ├── raw/
│   │       │   │   └── movies.json
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── .gitignore
├── build.gradle.kts
├── README.md
└── settings.gradle.kts
```

A estrutura pode apresentar pequenas diferenças dependendo da organização adotada no projeto.

---

# ▶️ Como executar o projeto

## Requisitos

- Android Studio instalado;
- Android SDK configurado;
- Emulador Android ou dispositivo físico;
- Conexão com a internet para pesquisar no TMDB;
- Chave válida da API TMDB.

## Etapas

1. Clone o repositório:

```bash
git clone https://github.com/AlexLearnsTech/CineFlix.git
```

2. Abra o projeto no Android Studio.

3. Aguarde a sincronização do Gradle.

4. Adicione a chave ao arquivo `local.properties`:

```properties
TMDB_API_KEY=SUA_CHAVE_DA_API
```

5. Selecione um emulador ou dispositivo físico.

6. Execute o aplicativo pelo botão **Run**.

---

# 📸 Capturas de tela

As capturas podem ser armazenadas na pasta:

```text
docs/imagens
```

## Tela inicial

![Tela inicial do CineFlix](docs/imagens/tela-inicial.png)

## Pesquisa de filmes

![Pesquisa de filmes](docs/imagens/tela-pesquisa.png)

## Detalhes do filme

![Detalhes do filme](docs/imagens/tela-detalhes.png)

## Filmes favoritos

![Filmes favoritos](docs/imagens/tela-favoritos.png)

## Configurações

![Configurações](docs/imagens/tela-configuracoes.png)

> As imagens somente serão exibidas no GitHub depois que os respectivos arquivos forem adicionados à pasta `docs/imagens`.

---

# 🚀 Melhorias implementadas no Módulo 5

Durante o Módulo 5, foram implementadas as seguintes melhorias:

- Migração do projeto para o Android Studio;
- Desenvolvimento em Kotlin;
- Criação de múltiplas Activities;
- Navegação utilizando Intents;
- Envio do identificador do filme para a tela de detalhes;
- Criação de menu de opções;
- Implementação de `RecyclerView`;
- Implementação de `View Binding`;
- Leitura do catálogo local em `res/raw`;
- Utilização de `openRawResource()`;
- Pesquisa local por título e gênero;
- Pesquisa com ou sem acentos;
- Pesquisa enquanto o usuário digita;
- Pesquisa on-line na API do TMDB;
- Execução da pesquisa em thread separada;
- Proteção contra resultados antigos;
- Tratamento de erros de conexão;
- Utilização do catálogo local quando a API não está disponível;
- Salvamento da última pesquisa com `SharedPreferences`;
- Compartilhamento de preferências entre Activities;
- Salvamento de histórico em arquivo interno;
- Leitura de arquivos internos;
- Exportação de dados para armazenamento externo;
- Criação do banco de dados SQLite;
- Criação da classe `DatabaseHelper`;
- Utilização de `SQLiteOpenHelper`;
- Salvamento permanente dos filmes favoritos;
- Atualização da documentação do projeto;
- Publicação do código-fonte no GitHub.

---

# 📊 Resultado do progresso

Ao término desta etapa, o CineFlix passou a utilizar diferentes formas de persistência e recuperação de dados.

O aplicativo consegue:

- Armazenar preferências simples;
- Recuperar a última pesquisa;
- Compartilhar preferências entre Activities;
- Salvar pesquisas em um arquivo interno;
- Ler um catálogo da pasta `res/raw`;
- Consultar filmes pela internet;
- Continuar oferecendo resultados locais quando a API falha;
- Armazenar filmes favoritos em SQLite;
- Exportar dados para um arquivo externo.

Essas melhorias tornaram o CineFlix mais completo, organizado e próximo do funcionamento de um aplicativo Android real.

---

# 🔗 Repositório do projeto

O código-fonte e a documentação estão disponíveis em:

### [Acessar o CineFlix no GitHub](https://github.com/AlexLearnsTech/CineFlix)

Endereço:

```text
https://github.com/AlexLearnsTech/CineFlix
```

---

## 📝 Observação sobre o GitHub Classroom

O enunciado da atividade orientava a publicação por meio do GitHub Classroom.

Entretanto, não foi disponibilizado pela instituição um link de acesso para uma sala ou atividade. Além disso, a criação de novas salas não estava disponível no momento da publicação.

Por esse motivo, o CineFlix foi publicado em um repositório convencional do GitHub, mantendo o código-fonte, o README e os demais arquivos disponíveis para consulta e avaliação.

---

# 🔮 Possíveis melhorias futuras

- Autenticação de usuários;
- Criação de perfis;
- Sincronização dos favoritos em nuvem;
- Exibição de trailers;
- Paginação dos resultados;
- Recomendações personalizadas;
- Testes unitários;
- Testes de interface;
- Melhorias de acessibilidade;
- Publicação do aplicativo.

---

# 🎓 Finalidade acadêmica

Este projeto foi desenvolvido para fins acadêmicos, com o objetivo de aplicar conhecimentos de desenvolvimento Android e persistência de dados.

Os dados relacionados aos filmes pesquisados pela internet são fornecidos pela API do **The Movie Database — TMDB**.

O projeto não possui vínculo oficial com a Netflix ou com o TMDB.

---

## Desenvolvido por

**Alex Magalhães Santos**
