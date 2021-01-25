NAME := main
FILE := ${NAME}.tex

default: build

build:
	pdflatex ${FILE}

aclean:
	rm -f -v *.aux
clean:
	rm -f ${NAME}.aux
	rm -f ${NAME}.log
	rm -f ${NAME}.toc
	rm -f ${NAME}.out
	rm -f ${NAME}.idx
	rm -f ${NAME}.ilg
	rm -f ${NAME}.ind
	rm -f -v *.aux
