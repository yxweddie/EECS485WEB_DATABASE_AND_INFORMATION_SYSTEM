JFLAGS = -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
	chmod +x eecs485pa5h.sh
	ln -s eecs485pa5h.sh eecs485pa5h   

CLASSES = \
        Hits.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class 
run: classes
	java Hits