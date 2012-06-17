package easyzk.exception;

public class EasyZkException extends Exception {

    public EasyZkException(Throwable e){
        super(e);
    }
    public EasyZkException(String msg){
        super(msg);
    }

    public EasyZkException(String msg, Throwable e){
        super(msg, e);
    }

}
