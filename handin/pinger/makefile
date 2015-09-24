JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = Pinger.java UDPManager.java UDPMessage.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
