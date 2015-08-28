package ch.ntb.inf.kmip.stub.transport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import ch.ntb.inf.kmip.stub.transport.KMIPStubTransportLayerHTTPSLocalHost.AliasKeyManager;
import ch.ntb.inf.kmip.utils.KMIPUtils;

public class KMIPStubTransportLayerTLS implements
        KMIPStubTransportLayerInterface {


    private static final Logger logger = Logger.getLogger(KMIPStubTransportLayerTLS.class);

    private int PORT = 5696;						// default values
    private String targetHostname = "localhost";	// default values

    private String keyStoreFileName;
    private String keyStorePassword;
    private String trustStoreFileName;
    private String trustStorePassword;
    private String alias = null;



    public ArrayList<Byte> send(ArrayList<Byte> al) {
        logger.info("KLMSClient Request Thread: " + Thread.currentThread());
        try {

            // create key and trust managers
            KeyManager[] keyManagers = createKeyManagers(keyStoreFileName, keyStorePassword, alias);
            TrustManager[] trustManagers = createTrustManagers(trustStoreFileName, trustStorePassword);
            // init context with managers data
            SSLSocketFactory factory = initItAll(keyManagers, trustManagers);
            // execute Post
            String responseString = executePost(targetHostname, PORT, factory, al);
            return(KMIPUtils.convertHexStringToArrayList(responseString));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void setTargetHostname(String value) {
        int split = value.indexOf(":");
        this.targetHostname = value.substring(0,split);
        this.PORT = Integer.parseInt(value.substring(split+1, value.length()));
        logger.info("Connection to: "+targetHostname+":"+PORT);
        logger.info("Using TLS v1.2");
        //logger.info("Using TLS v1.1");

    }

    @Override
    public void setKeyStoreLocation(String property) {
        keyStoreFileName = property;
        trustStoreFileName = property;
    }

    @Override
    public void setKeyStorePW(String property) {
        keyStorePassword = property;
        trustStorePassword = property;

    }

    private static String executePost(String hostName, int port, SSLSocketFactory sslSocketFactory, ArrayList<Byte> request) throws IOException {
        // Number of bytes read
        int iBytes = 0;
        // Byte array to read into
        byte[] bResponse = new byte[8192];

        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(hostName, port);


        try{


            // Send request
            DataOutputStream outToServer = new DataOutputStream(sslSocket.getOutputStream());
            // Prepare data to send
            byte[] b = new byte[request.size()];
            for(int i=0; i<request.size();i++){
                b[i]=request.get(i);
            }
            // Send data
            outToServer.write(b);
            outToServer.flush();

            logger.info("Data transmitted!");

            // Close output signalize EOF
            //sslSocket.shutdownOutput();
            // some stuff

            //wr.write( KMIPUtils.toByteArray(request));

           // wr.flush();


            // Get Response
            InputStream is = new DataInputStream(sslSocket.getInputStream());
            iBytes = is.read(bResponse);


            return(KMIPUtils.convertByteStringToHexString(bResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (sslSocket != null) {
                sslSocket.close();
            }
        }
    }

    private static SSLSocketFactory initItAll(KeyManager[] keyManagers, TrustManager[] trustManagers)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        //SSLContext context = SSLContext.getInstance("TLSv1.1");

        context.init(keyManagers, trustManagers, null);
        return context.getSocketFactory();
    }



    private static KeyManager[] createKeyManagers(String keyStoreFileName, String keyStorePassword, String alias)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        //create Inputstream to keystore file
        java.io.InputStream inputStream = new java.io.FileInputStream(keyStoreFileName);
        //create keystore object, load it with keystorefile data
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(inputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());

        KeyManager[] managers;
        if (alias != null) {
            managers = new KeyManager[] {new KMIPStubTransportLayerHTTPSLocalHost().new AliasKeyManager(keyStore, alias, keyStorePassword)};
        } else {
            //create keymanager factory and load the keystore object in it
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword == null ? null : keyStorePassword.toCharArray());
            managers = keyManagerFactory.getKeyManagers();
        }
        //return
        return managers;
    }

    private static TrustManager[] createTrustManagers(String trustStoreFileName, String trustStorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        /*
        //create Inputstream to truststore file
        java.io.InputStream inputStream = new java.io.FileInputStream(trustStoreFileName);
        //create keystore object, load it with truststorefile data
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(inputStream, trustStorePassword == null ? null : trustStorePassword.toCharArray());

        //create trustmanager factory and load the keystore object in it
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        //return
        return trustManagerFactory.getTrustManagers();
         */

        X509TrustManager passthroughTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        return(new TrustManager[] { passthroughTrustManager });

    }

}