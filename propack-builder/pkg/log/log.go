package log

import (
	"fmt"
	"github.com/charmbracelet/lipgloss"
)

type Sink interface {
	Info(string)
	Warn(string)
	Error(string)
}

var warnStyle = lipgloss.NewStyle().Foreground(lipgloss.Color("11"))
var errorStyle = lipgloss.NewStyle().Foreground(lipgloss.Color("9"))

type StdSink struct {
}

func (s *StdSink) Info(msg string) {
	println("INFO -- " + msg)
}

func (s *StdSink) Warn(msg string) {
	println(warnStyle.Render("WARN -- " + msg))
}

func (s *StdSink) Error(msg string) {
	println(errorStyle.Render("WARN -- " + msg))
}

var sink = StdSink{}

func Info(a ...any) {
	sink.Info(fmt.Sprint(a...))
}

func Warn(a ...any) {
	sink.Warn(fmt.Sprint(a...))
}

func Error(a ...any) {
	sink.Error(fmt.Sprint(a...))
}
