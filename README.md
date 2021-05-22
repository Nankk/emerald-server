# イメージのビルド

```sh
git clone https://gitlab.com/q-portal/sword-world-tools/emerald-server.git
cd emerald-server
# docker build . -t nankk/emerald-server # これでも可能だが...
docker-compose build # docker-compose.ymlにbuildの指定をしてるので、これでイメージをビルドしてくれます
```

# 実行

リポジトリをクローンするか `docker-compose.yml` をダウンロードし、 `docker-compose.yml` のあるディレクトリで以下を実行してください。

    docker-compose up -d

起動完了後、ブラウザで `http://localhost:53000` にアクセスするとSwaggerによるAPI仕様書が閲覧できます。
