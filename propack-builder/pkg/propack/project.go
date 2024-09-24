package propack

import (
	"encoding/json"
	"os"
	"propack-builder/pkg/log"
)

type Description interface {
}

func UnmarshalDescription(data []byte) (Description, error) {

	return nil, nil
}

type PPProject struct {
	FileVersion int    `json:"FileVersion"`
	Name        string `json:"Name"`
	PackFormat  int    `json:"PackFormat"`
	Icon        string `json:"Icon"`
	//Description string `json:"Description"`
}

func TryLoad() {
	// Using this to test json

	fileData, err := os.ReadFile("./project.json")
	if err != nil {
		log.Error(err.Error())
		return
	}

	project := PPProject{}
	err = json.Unmarshal(fileData, &project)
	if err != nil {
		log.Error(err.Error())
		return
	}

	log.Info(project)
}
