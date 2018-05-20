#!/usr/bin/env bash
docker run \
    --runtime nvidia \
    --volume /etc/passwd:/etc/passwd:ro \
    --user "$(id -u)" \
    --volume "$HOME:$HOME" \
    --volume "$PWD:/mnt/project-root" \
    --workdir /mnt/project-root \
    --tty --interactive \
    --init \
    popatry/anaconda-cuda:python3-anaconda5.1.0-cuda9.0-cudnn7-runtime-ubuntu16.04 \
    "$@"
