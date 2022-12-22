# tremors
Tremors is a scala application to collect and organize information about earthquakes around the world.

## running with podman instead docker

Just set the following environment variable:

```

DOCKER_HOST="unix://$XDG_RUNTIME_DIR/podman/podman.sock"

```

It'll usually look like:

```

DOCKER_HOST=unix:///run/user/<UID>/podman/podman.sock

```
