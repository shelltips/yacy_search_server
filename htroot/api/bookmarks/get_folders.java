

import java.util.Iterator;

import de.anomic.data.bookmarksDB;
import de.anomic.data.userDB;
import de.anomic.http.httpRequestHeader;
import de.anomic.plasma.plasmaSwitchboard;
import de.anomic.server.serverObjects;
import de.anomic.server.serverSwitch;

public class get_folders {    
	
	private static final serverObjects prop = new serverObjects();
	private static plasmaSwitchboard sb = null;
	private static userDB.Entry user = null;
	private static boolean isAdmin = false;
	
	public static serverObjects respond(final httpRequestHeader header, final serverObjects post, final serverSwitch<?> env) {
        		
		prop.clear();
    	sb = (plasmaSwitchboard) env;
    	user = sb.userDB.getUser(header);   
    	isAdmin = (sb.verifyAuthentication(header, true) || user != null && user.hasRight(userDB.Entry.BOOKMARK_RIGHT));
    	    	    	
    	// set user name
    	final String username;
    	if(user != null) username=user.getUserName();
    	else if(isAdmin) username="admin";
    	else username = "unknown";
    	prop.putHTML("display_user", username);
    	
    	// set peer address    	
    	prop.put("display_address", sb.webIndex.seedDB.mySeed().getPublicAddress());
    	prop.put("display_peer", sb.webIndex.seedDB.mySeed().getName());
    	    	
    	String root = "/";  	
    	String[] foldername = null;
    	
    	// check for GET parameters
    	if (post != null){
    		if (post.containsKey("root")) {
        		if (post.get("root").equals("source") || post.get("root").equals("")) root = "/";
        		else if (post.get("root").startsWith("/")) root = post.get("root");
    			else root = "/" + post.get("root");
    		}
    	}
    	
    	Iterator<String> it = null;
    	
    	// loop through folderList
    	it = sb.bookmarksDB.getFolderList(root, isAdmin);    	
    	int n = root.split("/").length;
    	if (n == 0) n = 1;
    	int count = 0;
    	while (it.hasNext()) {    		   		
    		String folder = it.next();
    		foldername = folder.split("/");
    		if (foldername.length == n+1) {
	    		prop.put("folders_"+count+"_foldername", foldername[n]);
	    		prop.put("folders_"+count+"_expanded", "false");
	    		prop.put("folders_"+count+"_type", "folder");
	    		prop.put("folders_"+count+"_hash", folder);				//TODO: switch from pathString to folderHash
	    		prop.put("folders_"+count+"_url", "");					//TODO: insert folder url
	    		prop.put("folders_"+count+"_hasChildren", "true");		//TODO: determine if folder has children
	    		prop.put("folders_"+count+"_comma", ",");
	    		count++;
    		}
    	}
    	
    	// loop through bookmarkList
    	it = sb.bookmarksDB.getBookmarksIterator(root, isAdmin); 
    	bookmarksDB.Bookmark bm;
    	while (it.hasNext()) {
    		bm = sb.bookmarksDB.getBookmark(it.next());
    		// TODO: get rid of bmtype
    		if (post.containsKey("bmtype")) {    			 
    			if (post.get("bmtype").equals("title")) {
    				prop.put("folders_"+count+"_foldername", bm.getTitle());
    			} else if (post.get("bmtype").equals("href")) {
    				prop.put("folders_"+count+"_foldername", "<a href='"+bm.getUrl()+" 'target='_blank'>"+bm.getTitle()+"</a>");
    			} else {
    				prop.put("folders_"+count+"_foldername", bm.getUrl());
    			}
    		}    		
    		prop.put("folders_"+count+"_expanded", "false");
    		prop.put("folders_"+count+"_url", bm.getUrl());
    		prop.put("folders_"+count+"_type", "file");
    		prop.put("folders_"+count+"_hash", bm.getUrlHash());
    		prop.put("folders_"+count+"_hasChildren", "false");
    		prop.put("folders_"+count+"_comma", ",");
    		count++;    		
    	}   	
    	
    	count--;
    	prop.put("folders_"+count+"_comma", "");
    	count++;
    	prop.put("folders", count);
    	
        // return rewrite properties
        return prop;
	}	
}