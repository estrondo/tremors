FROM docker.io/library/arangodb:3.11.1

LABEL authors="rthoth!"

RUN mkdir /data && \
      arangod \
      --log trace \
      --database.directory=/data \
      --database.init-database \
      --database.password=tremors \
      --server.descriptors-minimum=1024 \
      --rocksdb.compression-type=none

ENTRYPOINT  arangod \
            --database.directory=/data \
            --server.descriptors-minimum=1024 \
            --log trace
