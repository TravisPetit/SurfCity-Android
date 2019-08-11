package de.fqsmedia.cedrik.surfcity_android.ssb.discovery;

import com.goterl.lazycode.lazysodium.utils.Key;
import de.fqsmedia.cedrik.surfcity_android.ssb.identity.Identity;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocalIdentity {
    private static final Pattern pattern = Pattern.compile("^net:(.*):(.*)~shs:(.*)$");

    private final Identity identity;
    private final InetSocketAddress inetSocketAddress;

    public LocalIdentity(String ip, String port, Identity identity){
        this.identity = identity;
        inetSocketAddress = new InetSocketAddress(ip, Integer.valueOf(port));
    }


    public static LocalIdentity fromString(String string){
        Matcher matcher = pattern.matcher(string);
        if(matcher.matches()){
            return new LocalIdentity(
                    matcher.group(1),
                    matcher.group(2),
                    Identity.Companion.fromPublicKey(Key.fromBytes(Base64.getDecoder().decode(matcher.group(3).getBytes())))
            );
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LocalIdentity that = (LocalIdentity) o;
        return toString().equals(that.toString());
    }

    @Override
    public String toString() {
        return "net:"+inetSocketAddress.getHostString()+":" + inetSocketAddress.getPort() + "~shs:" + Base64.getEncoder().encodeToString(identity.getPublicKey());
    }
}
