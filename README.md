# CineFlix

O CineFlix é um aplicativo Android desenvolvido em Kotlin no Android Studio como parte das atividades práticas da disciplina de desenvolvimento de aplicativos.

O projeto começou inicialmente no MIT App Inventor e depois foi recriado no Android Studio para permitir a implementação de recursos mais completos. Durante os módulos foram adicionadas funcionalidades de pesquisa de filmes, persistência de dados, banco SQLite, comunicação com APIs, mapas e geolocalização.

## Autor

Alex Magalhães Santos

---

## Sobre o projeto

A proposta do CineFlix é criar um aplicativo de filmes inspirado em plataformas de streaming.

Atualmente o aplicativo permite pesquisar filmes, consultar seus detalhes, adicionar filmes aos favoritos, armazenar histórico de pesquisas, salvar preferências do usuário e pesquisar cinemas próximos utilizando mapas e localização.

O projeto foi sendo atualizado de forma incremental conforme os conteúdos estudados nos módulos da disciplina.

Entre os principais recursos já implementados estão:

* Pesquisa de filmes por título e gênero;
* Pesquisa de filmes pela internet usando a API do TMDB;
* Catálogo local em JSON;
* Tela de detalhes dos filmes;
* Lista de favoritos;
* Banco de dados SQLite;
* Histórico de pesquisas;
* SharedPreferences;
* Arquivos internos e externos;
* Múltiplas Activities;
* Navegação com Intents;
* Google Maps;
* Localização atual do usuário;
* Monitoramento de localização;
* Geocodificação;
* Geocodificação reversa;
* Pesquisa de endereços;
* Pesquisa de cinemas próximos;
* Comunicação HTTP;
* Processamento de dados JSON.

---

# Pesquisa de filmes

O CineFlix possui uma pesquisa local e também uma pesquisa on-line.

Enquanto o usuário digita no campo de pesquisa, o aplicativo procura resultados no catálogo local. A pesquisa on-line é realizada quando o usuário confirma a busca pelo teclado.

A comunicação com o TMDB é feita pela classe:

```text
TmdbRepository
```

A consulta é executada em uma thread separada para evitar que a interface fique travada durante a comunicação com a internet.

Os resultados são apresentados em um `RecyclerView`.

---

## Pesquisa por título e gênero

A pesquisa local permite procurar filmes pelo título ou pelo gênero.

O texto também passa por uma normalização antes da comparação. Dessa forma, pesquisas com ou sem acentos podem apresentar o mesmo resultado.

Exemplo:

```text
Ação
acao
AÇÃO
Acao
```

Trecho utilizado para normalização:

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

## Pesquisa on-line no TMDB

Quando o usuário confirma uma pesquisa, o aplicativo consulta a API do The Movie Database.

A operação é executada fora da thread principal.

Caso a consulta seja concluída, os resultados recebidos são apresentados no aplicativo.

Também existe uma verificação para impedir que uma resposta antiga substitua uma pesquisa mais recente.

```kotlin
val textoAtual = binding.editSearch.text
    .toString()
    .trim()

if (textoAtual != termo) {
    return@runOnUiThread
}
```

Caso a Activity já tenha sido encerrada, a interface também não é atualizada:

```kotlin
if (isFinishing || isDestroyed) {
    return@runOnUiThread
}
```

---

## Funcionamento em caso de erro na internet

Se ocorrer algum erro durante a consulta ao TMDB, o problema é registrado no Logcat.

```kotlin
Log.e(
    "CineFlixTMDB",
    "Erro ao consultar o TMDB",
    e
)
```

Nesse caso, o aplicativo tenta continuar utilizando o catálogo local.

Isso permite que parte da pesquisa continue disponível mesmo quando a consulta on-line não funciona.

---

# Catálogo local

O aplicativo também possui um catálogo armazenado no arquivo:

```text
app/src/main/res/raw/movies.json
```

A leitura é realizada através de:

```kotlin
resources.openRawResource(R.raw.movies)
```

A classe `MovieRepository` é responsável por carregar os dados e transformá-los em objetos utilizados pelo aplicativo.

---

# Detalhes do filme

Ao selecionar um filme, o usuário é direcionado para a `DetalhesActivity`.

A `MainActivity` envia o ID do filme através de uma `Intent`.

```kotlin
val intent = Intent(
    this,
    DetalhesActivity::class.java
)

intent.putExtra("movie_id", filme.id)

startActivity(intent)
```

A tela de detalhes utiliza esse identificador para localizar e apresentar as informações do filme escolhido.

---

# Filmes favoritos

Os filmes favoritos são armazenados em um banco de dados SQLite.

A classe responsável é:

```text
DatabaseHelper
```

Ela utiliza:

```kotlin
SQLiteOpenHelper
```

O banco criado pelo aplicativo é:

```text
cineflix.db
```

A tabela principal é:

```text
favoritos
```

Entre as informações armazenadas estão:

* ID;
* Título;
* Gênero;
* Ano;
* Nota;
* Sinopse;
* URL do pôster.

A tela responsável por exibir os favoritos é a:

```text
FavoritosActivity
```

Os filmes permanecem salvos mesmo depois que o aplicativo é fechado.

---

# Histórico de pesquisas

As pesquisas realizadas são registradas em um arquivo interno.

A classe responsável por esse recurso é:

```text
HistoricoManager
```

Exemplo:

```kotlin
HistoricoManager.salvar(
    this,
    termo
)
```

Esse recurso permite manter um histórico das pesquisas realizadas pelo usuário.

---

# SharedPreferences

O CineFlix utiliza `SharedPreferences` para armazenar informações simples.

A classe responsável é:

```text
PreferencesManager
```

Entre os dados armazenados está a última pesquisa digitada.

Exemplo de salvamento:

```kotlin
PreferencesManager.setUltimaPesquisa(
    this@MainActivity,
    termo
)
```

Exemplo de recuperação:

```kotlin
val ultimaPesquisa =
    PreferencesManager.getUltimaPesquisa(this)
```

As mesmas preferências podem ser acessadas por diferentes Activities.

---

# Exportação de dados

Os filmes favoritos podem ser exportados para um arquivo CSV.

Exemplo:

```text
favoritos_cineflix.csv
```

O arquivo pode conter informações como:

* Título;
* Gêneros;
* Ano;
* Nota;
* Sinopse.

---

# Navegação entre telas

O projeto utiliza várias Activities.

## MainActivity

Tela principal do aplicativo.

Responsável pela pesquisa de filmes, catálogo local, comunicação com o TMDB e acesso ao menu principal.

## DetalhesActivity

Apresenta os dados completos do filme selecionado.

## FavoritosActivity

Apresenta os filmes armazenados no banco SQLite.

## ConfiguracoesActivity

Tela utilizada para as configurações e preferências do aplicativo.

## CinemasActivity

Tela adicionada no Módulo 7.

É responsável pelos recursos de:

* Google Maps;
* Localização;
* Geocodificação;
* Geocodificação reversa;
* Marcadores;
* Pesquisa de endereços;
* Monitoramento de localização;
* Pesquisa de cinemas próximos.

---

# Menu principal

O menu da `MainActivity` está localizado em:

```text
app/src/main/res/menu/menu_main.xml
```

Atualmente ele possui opções para:

* Favoritos;
* Cinemas próximos;
* Configurações.

A navegação entre as telas é realizada utilizando `Intent`.

---

# Módulo 7 - Mapas e geolocalização

No Módulo 7 foram adicionados recursos relacionados a mapas, localização e comunicação com serviços web.

A principal funcionalidade criada foi a tela:

```text
Cinemas próximos
```

Ela pode ser acessada pelo menu principal do CineFlix.

---

## Google Maps

O mapa é exibido utilizando o Google Maps SDK for Android.

O layout utiliza um:

```text
SupportMapFragment
```

A Activity implementa:

```text
OnMapReadyCallback
```

O mapa é iniciado através de:

```kotlin
mapFragment.getMapAsync(this)
```

---

## Localização atual

A localização do usuário é obtida utilizando:

```text
FusedLocationProviderClient
```

O aplicativo solicita as permissões:

```text
ACCESS_COARSE_LOCATION
ACCESS_FINE_LOCATION
```

Quando a permissão é concedida, o mapa mostra o ponto azul indicando a posição atual do usuário.

A posição também é utilizada para centralizar o mapa.

---

## Obtenção da localização

Para obter uma posição atual, o aplicativo utiliza:

```kotlin
getCurrentLocation()
```

com:

```kotlin
Priority.PRIORITY_HIGH_ACCURACY
```

Também é utilizado:

```kotlin
lastLocation
```

quando existe uma posição anterior disponível.

---

## Monitoramento da localização

Enquanto a tela de cinemas está aberta, o CineFlix pode acompanhar alterações na posição.

Para isso são utilizados:

```text
LocationRequest
LocationCallback
LocationResult
```

As atualizações são iniciadas com:

```kotlin
requestLocationUpdates()
```

e interrompidas quando a tela deixa de ficar ativa:

```kotlin
removeLocationUpdates()
```

Dessa forma, o aplicativo não continua solicitando localização sem necessidade quando o usuário sai da tela.

---

## Latitude e longitude

A posição atual também é exibida na tela.

Exemplo:

```text
Sua localização: -19.91670, -43.93450
```

Durante o desenvolvimento, a localização do Android Emulator foi simulada através do ADB.

Exemplo:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" emu geo fix -43.9345 -19.9167
```

Nesse comando a longitude é informada antes da latitude.

---

# Interação com o mapa

O usuário pode tocar em um ponto do mapa para selecionar um local.

O recurso utiliza:

```kotlin
googleMap.setOnMapClickListener
```

Quando o usuário toca no mapa:

1. As coordenadas são capturadas;
2. Um marcador é criado;
3. O mapa é movimentado até o ponto;
4. O endereço correspondente é pesquisado;
5. O resultado aparece na tela.

---

# Geocodificação reversa

A geocodificação reversa transforma coordenadas em um endereço.

O fluxo utilizado é:

```text
Latitude e longitude
        ↓
Geocoder
        ↓
Endereço
```

Quando o usuário toca no mapa, o CineFlix tenta identificar o endereço correspondente.

O resultado é apresentado na tela e também pode ser mostrado no marcador.

---

# Pesquisa de endereço

Também foi implementado o processo inverso.

O usuário pode digitar um local como:

```text
Praça Sete, Belo Horizonte
```

ou:

```text
Mineirão, Belo Horizonte
```

O `Geocoder` tenta transformar o texto informado em latitude e longitude.

Depois disso, o CineFlix:

* Move o mapa;
* Cria um marcador;
* Mostra o endereço;
* Mostra as coordenadas.

---

# Controle do teclado

Durante a pesquisa de endereços, o teclado virtual é fechado automaticamente depois que a busca é iniciada.

Isso foi feito para evitar que o teclado ocupasse grande parte da tela e escondesse o mapa.

Foram utilizados:

```text
WindowCompat
WindowInsetsCompat
```

---

# Pesquisa de cinemas próximos

A tela possui um botão:

```text
Buscar cinemas próximos
```

A pesquisa utiliza como referência a localização atual do usuário.

Atualmente a consulta procura cinemas em um raio de aproximadamente:

```text
5 km
```

---

# OpenStreetMap e Overpass API

Os cinemas são obtidos através dos dados do OpenStreetMap utilizando a Overpass API.

A consulta procura locais classificados como:

```text
amenity=cinema
```

São considerados elementos dos tipos:

```text
node
way
relation
```

A classe responsável pela consulta é:

```text
CinemaRepository
```

---

# Comunicação HTTP

A `CinemaRepository` realiza uma consulta HTTP à Overpass API.

A conexão utiliza:

```text
HttpURLConnection
```

A operação é executada em uma thread separada.

O fluxo simplificado é:

```text
Localização atual
       ↓
CinemaRepository
       ↓
Requisição HTTP
       ↓
Overpass API
       ↓
Resposta JSON
       ↓
CinemaMapa
       ↓
Marcadores no mapa
```

---

# Processamento do JSON

A resposta da Overpass API é recebida em JSON.

Para interpretar os dados são utilizados:

```text
JSONObject
JSONArray
```

Cada cinema encontrado é convertido para um objeto do tipo:

```text
CinemaMapa
```

---

# Classe CinemaMapa

A classe `CinemaMapa` representa um cinema encontrado na consulta.

Ela possui informações como:

* ID do OpenStreetMap;
* Nome;
* Latitude;
* Longitude;
* Endereço;
* Distância aproximada.

---

# Distância dos cinemas

O aplicativo calcula uma distância aproximada entre a posição atual do usuário e cada cinema.

O cálculo utiliza:

```kotlin
Location.distanceBetween()
```

O resultado é convertido de metros para quilômetros.

Exemplo:

```text
Cinema Santa Tereza • 2,1 km
```

A distância apresentada é em linha reta e não representa uma rota por ruas.

---

# Principais classes do projeto

```text
MainActivity.kt
DetalhesActivity.kt
FavoritosActivity.kt
ConfiguracoesActivity.kt
CinemasActivity.kt
Movie.kt
MovieAdapter.kt
MovieRepository.kt
TmdbRepository.kt
CinemaMapa.kt
CinemaRepository.kt
PreferencesManager.kt
HistoricoManager.kt
DatabaseHelper.kt
```

### MainActivity

Controla a tela principal e as pesquisas.

### DetalhesActivity

Apresenta os dados do filme escolhido.

### FavoritosActivity

Apresenta os filmes armazenados no SQLite.

### ConfiguracoesActivity

Gerencia as preferências do aplicativo.

### CinemasActivity

Controla o Google Maps e os recursos de localização.

### MovieRepository

Carrega os filmes locais e mantém informações de filmes encontrados on-line.

### TmdbRepository

Responsável pela comunicação com o TMDB.

### CinemaRepository

Responsável pela consulta de cinemas através da Overpass API.

### CinemaMapa

Representa os cinemas encontrados.

### PreferencesManager

Gerencia os dados salvos em `SharedPreferences`.

### HistoricoManager

Gerencia o histórico em arquivo interno.

### DatabaseHelper

Gerencia o banco SQLite.

---

# Tecnologias utilizadas

O projeto utiliza atualmente:

* Android Studio;
* Kotlin;
* XML;
* Android SDK;
* View Binding;
* RecyclerView;
* Intent;
* SharedPreferences;
* SQLite;
* SQLiteOpenHelper;
* JSON;
* JSONObject;
* JSONArray;
* HTTP;
* HttpURLConnection;
* TMDB API;
* Google Maps SDK for Android;
* Google Play Services Location;
* FusedLocationProviderClient;
* LocationRequest;
* LocationCallback;
* Geocoder;
* OpenStreetMap;
* Overpass API;
* Threads;
* Git;
* GitHub.

---

# Configuração das chaves de API

As chaves utilizadas no projeto não ficam diretamente no código-fonte.

Elas devem ser configuradas no arquivo:

```text
local.properties
```

Exemplo:

```properties
TMDB_API_KEY=SUA_CHAVE_TMDB
MAPS_API_KEY=SUA_CHAVE_GOOGLE_MAPS
```

O arquivo `local.properties` não deve ser publicado no GitHub.

---

## TMDB

A chave do TMDB é disponibilizada ao aplicativo através do:

```kotlin
BuildConfig.TMDB_API_KEY
```

---

## Google Maps

A chave do Maps é enviada ao `AndroidManifest.xml` utilizando um `manifestPlaceholder`.

No `build.gradle.kts`:

```kotlin
manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
```

No `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

No Google Cloud, a chave foi configurada com restrições para o aplicativo Android e para o Maps SDK for Android.

---

# Estrutura resumida do projeto

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
│   │       │       ├── CinemasActivity.kt
│   │       │       ├── Movie.kt
│   │       │       ├── MovieAdapter.kt
│   │       │       ├── MovieRepository.kt
│   │       │       ├── TmdbRepository.kt
│   │       │       ├── CinemaMapa.kt
│   │       │       ├── CinemaRepository.kt
│   │       │       ├── PreferencesManager.kt
│   │       │       ├── HistoricoManager.kt
│   │       │       └── DatabaseHelper.kt
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   ├── layout/
│   │       │   ├── menu/
│   │       │   ├── raw/
│   │       │   │   └── movies.json
│   │       │   ├── values/
│   │       │   └── xml/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── .gitignore
├── build.gradle.kts
├── README.md
└── settings.gradle.kts
```

---

# Como executar o projeto

## Requisitos

* Android Studio instalado;
* Android SDK configurado;
* Emulador ou dispositivo Android;
* Conexão com a internet;
* Chave da API TMDB;
* Chave do Google Maps;
* Maps SDK for Android habilitado no Google Cloud.

## Passos

Clone o repositório:

```bash
git clone https://github.com/AlexLearnsTech/CineFlix.git
```

Abra o projeto no Android Studio.

Aguarde a sincronização do Gradle.

Configure no `local.properties`:

```properties
TMDB_API_KEY=SUA_CHAVE_TMDB
MAPS_API_KEY=SUA_CHAVE_GOOGLE_MAPS
```

Depois selecione um emulador ou dispositivo e execute o aplicativo.

Na primeira vez que abrir a tela de cinemas, autorize o acesso à localização.

---

# Capturas de tela

As imagens do projeto podem ser armazenadas na pasta:

```text
docs/imagens
```

Exemplos:

```text
tela-inicial.png
tela-pesquisa.png
tela-detalhes.png
tela-favoritos.png
tela-configuracoes.png
tela-mapa.png
tela-geocodificacao.png
tela-cinemas-proximos.png
```

---

# Melhorias implementadas no Módulo 5

Entre as principais implementações realizadas no Módulo 5 estão:

* Migração do projeto para o Android Studio;
* Desenvolvimento em Kotlin;
* Múltiplas Activities;
* Navegação com Intents;
* RecyclerView;
* View Binding;
* Catálogo em `res/raw`;
* Pesquisa local;
* Pesquisa on-line no TMDB;
* Threads;
* SharedPreferences;
* Histórico em arquivo interno;
* Armazenamento externo;
* SQLite;
* Favoritos;
* Exportação CSV.

---

# Melhorias implementadas no Módulo 7

No Módulo 7 foram implementados:

* Integração com Google Maps;
* Criação da `CinemasActivity`;
* Localização atual;
* Exibição da posição no mapa;
* Monitoramento de localização;
* Latitude e longitude;
* Interação com o mapa;
* Marcadores;
* Geocodificação;
* Geocodificação reversa;
* Pesquisa de endereços;
* Controle do teclado virtual;
* Consulta de cinemas próximos;
* OpenStreetMap;
* Overpass API;
* Requisições HTTP;
* Processamento de JSON;
* Classe `CinemaMapa`;
* Classe `CinemaRepository`;
* Cálculo de distância aproximada dos cinemas.

---

# Situação atual do projeto

Depois das melhorias realizadas, o CineFlix passou a reunir diferentes conceitos estudados durante a disciplina.

Atualmente o aplicativo trabalha com:

* Persistência de dados;
* Banco SQLite;
* Arquivos internos e externos;
* APIs externas;
* JSON;
* Comunicação HTTP;
* Mapas;
* Localização;
* Geocodificação;
* Monitoramento;
* Serviços web.

A funcionalidade de cinemas próximos também ajudou a integrar os recursos de mapa ao objetivo principal do aplicativo, em vez de utilizar o Google Maps apenas como uma demonstração isolada.

---

# Repositório

O projeto está disponível em:

https://github.com/AlexLearnsTech/CineFlix

---

# Observação sobre o GitHub Classroom

O enunciado da atividade orientava a publicação através do GitHub Classroom.

Entretanto, não foi disponibilizado anteriormente um link específico para uma sala ou atividade. Por esse motivo, o projeto foi mantido em um repositório normal do GitHub, contendo o código e a documentação necessária para avaliação.

---

# Possíveis melhorias futuras

Algumas melhorias que ainda podem ser desenvolvidas futuramente são:

* Autenticação de usuários;
* Perfis;
* Sincronização dos favoritos em nuvem;
* Trailers;
* Recomendações personalizadas;
* Paginação;
* Rotas até os cinemas;
* Informações adicionais sobre os cinemas;
* Testes unitários;
* Testes de interface;
* Melhorias de acessibilidade;
* Melhorias visuais;
* Publicação do aplicativo.

---

# Finalidade acadêmica

O CineFlix foi desenvolvido para fins acadêmicos com o objetivo de aplicar os conteúdos estudados durante a disciplina de desenvolvimento Android.

Os dados dos filmes pesquisados pela internet são fornecidos pela API do The Movie Database.

Os dados utilizados para localizar cinemas são obtidos através do OpenStreetMap utilizando a Overpass API.

O mapa é exibido através do Google Maps SDK for Android.

O projeto não possui vínculo oficial com Netflix, TMDB, Google ou OpenStreetMap.

---

Desenvolvido por Alex Magalhães Santos.
