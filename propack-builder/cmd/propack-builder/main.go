package main

import (
	"propack-builder/pkg/log"
	"propack-builder/pkg/propack"
)

func main() {
	log.Info("Running ProPack Builder")
	propack.TryLoad()
}
