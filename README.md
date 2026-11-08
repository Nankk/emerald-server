# emerald-server

TRPG「ソード・ワールド 2.5」のキャラクター作成・管理ツール Emerald のサーバー（Clojure / Ring / Reitit / PostgreSQL）。
ツール全体の説明と画面は [emerald-frontend](https://github.com/Nankk/emerald-frontend) の README にあります。

## 構成

- `src/emerald_server/handler/`: REST API（characters、items、tables、cbb = キャラクター作成の人物像データ）。Reitit で定義し、Swagger UI で仕様を公開
- `src/emerald_server/models/`: キャラクターと能力値のデータモデル（clojure.spec で検証）
- `src/emerald_server/parser/excel.clj`: ルールデータ（Excel ブック）の読み込み。docjure を自分でフォークした版（`org.clojars.nankk/docjure`）を使用
- `resources/data/`: ルールデータの置き場。**ルールブック由来の著作物のため、データ本体は同梱していません**（履歴からも除去済み）。列構成と読み込み規則は [resources/data/README.org](resources/data/README.org)

## 動かす

ルールデータ（`resources/data/*.xlsx`）を自分で用意してから。

```sh
docker-compose build     # イメージのビルド（Dockerfile を使う）
docker-compose up -d     # PostgreSQL とサーバーを起動
```

起動後、`http://localhost:53000` で Swagger の API 仕様書が見られます。docker-compose の DB パスワードはローカル用の固定値です。

開発時は Leiningen（`project.clj`）で REPL を立てます。

## 分担

API とデータモデルは Nankk（本人）、Dockerfile・docker-compose・GitLab CI は友人 2 名。コミット履歴がそのまま記録です。`.gitlab-ci.yml` は GitLab で運用していた当時のままです。
