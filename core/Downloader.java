package core;

import message.Bitfield;
import message.Piece;
import utils.Datafile;
import utils.Logger;
import utils.MessageSender;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles this peer downloading from other peers.
 */
public class Downloader {

    private Logger logger;

    private long initTime;
    public Downloader(Logger logger) {
        this.logger = logger;
    }

    public void receiveChoke(Connection connection, Peer peer) {
        connection.getDownloadState().setChoked(true);
        logger.log("receive CHOKE from " + peer);
    }

    public void receiveUnchoke(Connection connection, Peer peer, Datafile datafile) {
        connection.getDownloadState().setChoked(false);
        logger.log("receive UNCHOKE from " + peer);
        requestFirstAvailPiece(connection, peer, datafile);   // request first available piece
    }

    public void receivePiece(Connection connection,
                             Peer peer,
                             ConcurrentMap<Peer, Connection> connections,
                             Datafile datafile,
                             Piece piece) {
        logger.log(String.format("receive PIECE for pieceIndex:%d from " + peer, piece.getPieceIndex()));
        datafile.getBitfield().setPieceToCompleted(piece.getPieceIndex());                  // 1. update bitfield
        datafile.writePiece(piece.getBlock(), piece.getPieceIndex());                       // 2. write piece
        connection.incrementBytesDownloaded(piece.getBlock().length);                       // 3. update bytes downloaded
        for (Map.Entry<Peer, Connection> peerConnection : connections.entrySet()) {         // 4. broadcast Have new piece to all peers
            if (!peerConnection.getValue().equals(connection)) {
                MessageSender.sendHave(peerConnection.getValue(), peer, logger, piece.getPieceIndex());
                logger.log("send HAVE to " + peerConnection.getKey());
            }
        }
        requestFirstAvailPiece(connection, peer, datafile);                                 // 5. request next piece
    }

    public void receiveBitfield(Connection connection, Peer peer, Bitfield bitfield, Datafile datafile) {
        logger.log("receive BITFIELD " + bitfield + " from " + peer);
        initTime = System.currentTimeMillis();
        connection.setBitfield(bitfield);               // set peer's bitfield
        if (!datafile.isCompleted()) {
            MessageSender.sendInterested(connection, peer, logger);
        }
    }

    /**
     * Requests from peer the first missing piece that peer has.
     */
    private void requestFirstAvailPiece(Connection connection, Peer peer, Datafile datafile) {
		if (datafile.isCompleted()) {
			long timeDescarga = (System.currentTimeMillis() - initTime)/1000;
			logger.log("Datafile is complete" );
			logger.log("TIEMPO: " + timeDescarga);
			logger.log("FECHA: " + LocalDateTime.now());
			//Fecha,tiempo de descarga, nombre del archivo, tamanio de archivo, numero de chunks, conexion exitosa
			logger.logData(LocalDateTime.now()+","+timeDescarga+","+datafile.getFilename()+","+datafile.getFileLength()+","+datafile.getNumPieces()+","+"OK");
			
			return;
		}
		for (int i = 0; i < datafile.getNumPieces(); i++) {
			if (datafile.getBitfield().missingPiece(i) && connection.getBitfield().hasPiece(i)) {
				MessageSender.sendRequest(connection, peer, logger, i, datafile.getPieceLength()); // request entire piece
				break;
			}
		}
	}

    public Logger getLogger() {
        return logger;
    }
}