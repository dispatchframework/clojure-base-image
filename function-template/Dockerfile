ARG IMAGE
FROM ${IMAGE}

ARG FUNCTION_SRC=function.txt

# FUNCTION_MODULE env var is set in the base-image
COPY ${FUNCTION_SRC} ${FUNCTION_MODULE}

RUN echo "  (require '[func-server.main :as m]) (m/run-server function)" >> ${FUNCTION_MODULE}

CMD clojure ${FUNCTION_MODULE}
