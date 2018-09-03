shuzu.org Generator
===================

```
cd website
npm install
npm run build

cd ..
```

Then choose any of:

- `./gradlew FetchGithubData`
- `./gradlew SyncLocalRepos`
- `./gradlew RenderLiveSearchData`
- `./gradlew BuildWebsiteProject`
- `./gradlew SiteGenerator`

View local site:

```
npm install -g http-server
cd cache/site/
http-server . -c-1 -o
```

If everything is OK, push `cache/site` to shuzu.org
