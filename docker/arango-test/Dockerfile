FROM arangodb/arangodb:3.10.0


RUN   mkdir /data && \
      arangod \
      --log trace \
      --database.directory=/data \
      --database.init-database \
      --database.password=123456789 \
      --server.descriptors-minimum=1024 \
      --rocksdb.compression-type=none

ENTRYPOINT  arangod \
            --database.directory=/data \
            --server.descriptors-minimum=1024 \
            --log trace