package io.darkblock.rest;

import static spark.Spark.*;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import io.darkblock.tools.Tools;

public class ApiService {
   public static void main(String[] args) {
       
       get("/codeinput/:code", (req,res)->{
           return processInput( req.params(":code") );
       });
       
       get("/codeset/:code/:wallet", (req,res)->{
           return associateCode( req.params(":code"), req.params(":wallet") );
       });
	   
       get("/codepoller/:code", (req,res)->{
           return processCode( req.params(":code") );
       });
       
       get("/triggergeneration/:wallet", (req,res)->{//trigger this from the web side or for test purposes
           return triggerGeneration( req.params(":wallet") );
       });
       
       get("/artkeygenerator/:wallet", (req,res)->{//the poller
           return generateArtKey( req.params(":wallet") );
       });
       
       post("/artkeypost", (req,res)->{//this needs to be more secure obviously... can't have just anyone generating keys.
    	   System.err.println( "key: " + req.queryParams("key") );
    	   System.err.println( "wallet: " + req.queryParams("wallet") );
    	   System.err.println( "artid: " + req.queryParams("artid") );
           return saveArtKeys( req.queryParams("key"), req.queryParams( "wallet" ), req.queryParams( "artid" ) );
       });
       
       get("/getartkey/:wallet", (req,res)->{//the poller
           return getArtKey( req.params(":wallet") );
       });
       
       get("/transferpoll/:wallet", (req,res)->{
           return transferPoll( req.params(":wallet") );
       });
       
       get("/transferconfirmation/:wallet_from/:wallet_to/:artid", (req,res)->{
           return transferConfirmation( req.params(":wallet_from"), req.params(":wallet_to"), req.params(":artid") );
       });
       
       get("/transferkey/:wallet_from/:wallet_to/:rsa_key", (req,res)->{
           return transferKey( req.params(":wallet_from"), req.params(":wallet_to"), req.params(":rsa_key") );
       });
       
       get("/transferartkey/:wallet_from/:wallet_to/:artid/:art_key", (req,res)->{
           return transferArtKey( req.params(":wallet_from"), req.params(":wallet_to"), req.params(":artid"), req.params(":art_key") );
       });
       
       get("/transferartkeypost", (req,res)->{
    	   System.err.println( "posted art key" );
    	   System.err.println( "wallet_from: " + req.queryParams("wallet_from") );
    	   System.err.println( "wallet_to: " + req.queryParams("wallet_to") );
    	   System.err.println( "artid: " + req.queryParams("artid") );
    	   System.err.println( "key: " + req.queryParams("art_key") );
           return transferArtKey( req.queryParams("wallet_from"), req.queryParams("wallet_to"), req.queryParams("artid"), req.queryParams("art_key") );
       });
       
       post("/transfer", (req,res)->{//this needs to be more secure obviously... can't have just anyone generating keys.
    	   System.err.println( "wallet_from: " + req.queryParams("wallet_from") );
    	   System.err.println( "wallet_to: " + req.queryParams("wallet_to") );
    	   System.err.println( "artid: " + req.queryParams("artid") );
           return triggerSale( req.queryParams("wallet_from"), req.queryParams( "wallet_to" ), req.queryParams( "artid" ) );
       });
       
       
       //new protocol for keys
       get("/savekey/:wallet/:artid/:art_key", (req,res)->{
           return saveKey( req.params(":wallet"), req.params(":artid"), req.params(":art_key") );
       });
       
       get("/getkey/:wallet/:artid", (req,res)->{
           return getKey( req.params(":wallet"), req.params(":artid") );
       });
   }
   
   private static String saveKey( String wallet, String artid, String artKey ){
	   System.err.println( "saving " + wallet + " : " + artid + " : " + artKey );
	   if( artKey == null || wallet == null || artid == null ) {
		   return "{\"saved\":false}";
	   }
	   new File("keys/" + wallet).mkdirs();
	   try {
		   Tools.writeFile( "keys/" + wallet + "/" + artid + ".key", URLDecoder.decode( artKey ) );
	   }
	   catch( Exception e ) {
		   e.printStackTrace();
		   return "{\"saved\":false}";
	   }
	   return "{\"saved\":true}";
   }
   
   private static String getKey( String wallet, String artid ) {
	   String key = null;
		try {
			key = Tools.getFile( "keys/" + wallet + "/" + artid + ".key" );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "{\"key\":null}";
		}
		
		if( key == null ) {
		   return "{\"key\":null}";
		}
		return "{\"key\":\"" + key + "\"}";
   }
   
   private static Set<String> triggerMap = new HashSet<>();
   private static Map<String,String> codeMap = new HashMap<>();
   private static Map<String,String[]> keyMap = new HashMap<>();
   
   private static String processInput( String code ){
	   //codeMap.put( code, "fmTpIBFrCbAyUjV-f3VOq7Szs5RaMovb1Pf9MlUnjVk" );
	   return "{\"status\":\"success\"}";
   }
   
   private static String processCode( String code ){
	   System.err.println( "got code " + code );
	   if( codeMap.get( code ) != null ) {
		   return "{\"wallet\":\"" + codeMap.remove( code ) + "\"}";//wallet id
	   }
	   return "{\"wallet\": null}";
   }
   
   private static String triggerGeneration( String wallet ){
	   triggerMap.add( wallet );
	   return "{\"success\":true}";
   }
   
   private static String generateArtKey( String wallet ){
	   if( triggerMap.contains( wallet ) ) {
		   return "{\"generate\":true}";
	   }
	   return "{\"generate\":false}";
   }
   
   private static String saveArtKeys( String key, String wallet, String artid ){
	   System.err.println( "saving " + key + " : " + wallet + " : " + artid );
	   if( key == null || wallet == null || artid == null ) {
		   return "{\"saved\":false}";
	   }
	   keyMap.put( wallet, new String[]{ artid, key } );
	   triggerMap.remove( wallet );
	   return "{\"saved\":true}";
   }
   
   private static String associateCode( String code, String wallet ){
	   System.err.println( "associating code " + code + " with wallet" + wallet );
	   codeMap.put( code, wallet );
	   return "{\"saved\":true}";
   }
   
   private static String getArtKey( String wallet ){
	   System.err.println( "getting art key for : " + wallet );
	   String[] artkey = keyMap.remove( wallet );
	   if( artkey != null )
		   return "{\"artid\":\"" + artkey[0] + "\", \"key\":\"" + artkey[1] + "\"}";
	   return "{\"key\":null}";
   }
   
   private static Map<String,Map> transferOutMap = new HashMap<>();
   private static Map<String,Map> transferInMap = new HashMap<>();
   private static Map<String,String> rsaKeyTransferMap = new HashMap<>();
   private static Map<String,String> artKeyTransferMap = new HashMap<>();
   private static Map<String,String> destroyArtMap = new HashMap<>();
   
   private static String transferPoll( String wallet ){
	   if( transferOutMap.get( wallet ) != null && rsaKeyTransferMap.get( wallet ) != null ) {
		   Map out = transferOutMap.get( wallet );
		   out.put( "rsa_key", rsaKeyTransferMap.get(wallet) );
		   return new Gson().toJson( out );//wallet id
	   }
	   if( transferInMap.get( wallet ) != null && artKeyTransferMap.get( wallet ) != null ) {
		   Map out = transferInMap.get( wallet );
		   out.put( "art_key", artKeyTransferMap.get(wallet) );
		   return new Gson().toJson( out );//wallet id
	   }
	   if( transferInMap.get( wallet ) != null ) {
		   Map out = transferInMap.get( wallet );
		   return new Gson().toJson( out );//wallet id
	   }
	   if( destroyArtMap.get( wallet ) != null ) {
		   Map out = new HashMap();
		   out.put("transaction", "destroy");
		   out.put( "artid", destroyArtMap.remove(wallet) );
		   return new Gson().toJson( out );//wallet id		   
	   }
	   return "{\"wallet\": null}";
   }
   
   private static String transferKey( String walletFrom, String walletTo, String rsaKey ){
	   rsaKeyTransferMap.put( walletFrom, rsaKey );
	   transferInMap.remove( walletTo );
	   System.err.println( "transferring rsakey (" + rsaKey + ") from " + walletFrom + " to " + walletTo );
	   return "{\"saved\":true}";
   }
   
   private static String transferArtKey( String walletFrom, String walletTo, String artid, String artKey ){
	   System.err.println( "transferring art key (" + artKey + ") from " + walletFrom + " to " + walletTo + " for artid " + artid );
	   artKeyTransferMap.put( walletTo, artKey );
	   transferOutMap.remove(walletFrom);
	   Map out = new HashMap();
	   out.put( "transaction", "save" );
	   out.put( "artid", artid );
	   out.put( "artkey", artKey );
	   out.put( "wallet", walletFrom );
	   transferInMap.put( walletTo, out );
	   return "{\"saved\":true}";
   }
   
   private static String triggerSale( String walletFrom, String walletTo, String artid ){
	   System.err.println( "triggering sale from " + walletFrom + " to " + walletTo + " for " + artid );
	   if( walletFrom == null || walletTo == null || artid == null ) {
		   return "{\"c\":false}";
	   }
	   Map out = new HashMap();
	   out.put( "transaction", "sell" );
	   out.put( "artid", artid );
	   out.put( "wallet", walletFrom );
	   transferOutMap.put( walletFrom, out );
	   
	   Map in = new HashMap();
	   in.put( "transaction", "sendkey" );
	   in.put( "artid", artid );
	   in.put( "wallet", walletTo ); 
	   transferInMap.put( walletTo, in );
	   return "{\"saved\":true}";
   }

   private static String transferConfirmation( String walletFrom, String walletTo, String artid ){
	   System.err.println( "confirming transfer of art (" + artid+  ") from " + walletFrom + " to " + walletTo );
	   transferOutMap.remove(walletFrom);
	   transferInMap.remove(walletTo);
	   rsaKeyTransferMap.remove(walletFrom);
	   artKeyTransferMap.remove(walletTo);
	   destroyArtMap.put( walletFrom, artid );
	   return "{\"saved\":true}";
   }
}