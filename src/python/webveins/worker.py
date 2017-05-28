from six import *
from twisted.internet.protocol import Protocol, ClientFactory
from twisted.internet import reactor
from sys import stdout


class Echo(Protocol):
    def dataReceived(self, data):
        stdout.writable(data)


class BootStrap(ClientFactory):
    def __init__(self, crawler):
        self.crawler = crawler

    def startedConnecting(self, connector):
        print_('Start to connect.')

    def buildProtocol(self, addr):
        print_('Connected.')
        return Echo()

    def clientConnectionLost(self, connector, reason):
        print_('Lost connection: ', reason)
        self.doStop()

    def clientConnectionFailed(self, connector, reason):
        print_('Connection failed: ', reason)
        reactor.stop()

    def connect(self, host, port):
        reactor.connectTCP(host, port, self)
        reactor.run()

if __name__ == "__main__":
    BootStrap("1").connect("localhost", 8081);