package task

import (
	"sync"
)

type RunContext struct {
}

type Task interface {
	Run(*RunContext) error
}

type NameTaskPair struct {
	name string
	task Task
}

type Queue struct {
	queue []NameTaskPair
	mutex sync.Mutex
}

func NewQueue() *Queue {
	return &Queue{
		queue: make([]NameTaskPair, 0),
		mutex: sync.Mutex{},
	}
}

func (q *Queue) Add(name string, task Task) {
	q.mutex.Lock()
	defer q.mutex.Unlock()
	q.queue = append(q.queue, NameTaskPair{name, task})
}

func (q *Queue) Run(context *RunContext) bool {
	for {
		q.mutex.Lock()
		if len(q.queue) == 0 {
			q.mutex.Unlock()
			return true
		}
		task := q.queue[0]
		q.queue = q.queue[1:]
		q.mutex.Unlock()

		log.PrintInfo("> Task", task.name)
		err := task.task.Run(context)
		if err != nil {
			log.PrintError("> Task", task.name, "FAILED:", err)
			return false
		}
	}
}
