package main

import (
	"context"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"

	ctrace "github.com/Nordstrom/ctrace-go"
	chttp "github.com/Nordstrom/ctrace-go/http"
	"github.com/Nordstrom/ctrace-go/log"
	opentracing "github.com/opentracing/opentracing-go"
)

var httpClient = &http.Client{
	Transport: chttp.NewTracedTransport(&http.Transport{}),
}

func send(ctx context.Context, u string, r string) ([]byte, error) {
	req, err := http.NewRequest("GET", u+"?region="+r, nil)
	if err != nil {
		return nil, err
	}
	res, err := httpClient.Do(req.WithContext(ctx))
	if err != nil {
		return nil, err
	}

	defer res.Body.Close()
	return ioutil.ReadAll(res.Body)
}

func gateway(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	u, _ := url.QueryUnescape(q.Get("url"))
	region := q.Get("region")

	body, err := send(r.Context(), u, region)
	if err != nil {
		w.WriteHeader(500)
		w.Write([]byte("ERROR!"))
	} else {
		w.WriteHeader(200)
		w.Write(body)
	}
}

func ok(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	region, _ := url.QueryUnescape(q.Get("region"))
	msg := hello(r.Context(), region)
	w.Write([]byte(msg))
}

func hello(ctx context.Context, region string) string {
	span := opentracing.SpanFromContext(ctx)
	msg := fmt.Sprintf("Hello %v!", region)
	span.LogFields(log.Event("generate-msg"), log.Message(msg))

	return msg
}

func err(w http.ResponseWriter, r *http.Request) {

}

func initializeTracing() *os.File {
	logFile := os.Getenv("LOGFILE")

	if logFile == "" {
		opentracing.InitGlobalTracer(ctrace.New())
		return nil
	}

	writer, err := os.Create(logFile)
	if err != nil {
		fmt.Printf("Failed to create log file '%s'. Will fall back to logging only to stdout.\n", logFile)
		opentracing.InitGlobalTracer(ctrace.New())
		return nil
	}

	opentracing.InitGlobalTracer(ctrace.NewWithOptions(ctrace.TracerOptions{Writer: writer}))
	return writer
}

func main() {

	writer := initializeTracing()
	defer writer.Close()

	http.HandleFunc("/gw", gateway)
	http.HandleFunc("/ok", ok)
	http.HandleFunc("/err", err)

	fmt.Println("Hello Gateway Started on port 80...")
	e := http.ListenAndServe(":80", chttp.TracedHandler(http.DefaultServeMux))
	if e != nil {
		panic(e)
	}
}
