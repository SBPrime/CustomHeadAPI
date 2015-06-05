/*
 * CustomHeadAPI is a library plugin for Minecraft that allows simple cration of
 * player heads that use custom textures.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) CustomHeadAPI contributors:
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.customheadapi.implementation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.primesoft.customheadapi.CustomHeadApi;
import org.primesoft.customheadapi.IHeadCreator;
import org.primesoft.customheadapi.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class CustomHeadCreator implements IHeadCreator {
    private final Base64 m_base64 = new Base64();
    
    public void injectWhitelistUrl(String url) {

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'");
        }
        String[] whitelistedDomains = Reflection.get(YggdrasilMinecraftSessionService.class, String[].class,
                "WHITELISTED_DOMAINS", "failed to get whitelisted domains");
        String domain = uri.getHost();
        boolean foundDomain = false;
        for (String whitelistedDomain : whitelistedDomains) {
            if (domain.endsWith(whitelistedDomain)) {
                foundDomain = true;
                break;
            }
        }
        if (!foundDomain) {
            List<String> domains = Arrays.asList(whitelistedDomains);
            domains.add(uri.getHost());
            Reflection.set(YggdrasilMinecraftSessionService.class,
                    "WHITELISTED_DOMAINS", domains.toArray(new String[domains.size()]), "failed to inject whitelist domain");
        }
    }
    
    @Override
    public GameProfile createGameProfile(String url)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) 
        {
            CustomHeadApi.log("No property map found in GameProfile, can't continue.");
            return null;
        }
        
        injectWhitelistUrl(url);

        byte[] encodedData = m_base64.encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        propertyMap.put("textures", new Property("textures", new String(encodedData)));        
        
        return profile;
    }
    
    @Override
    public ItemStack createItemStack(String url) {
        GameProfile profile = createGameProfile(url);
        
        if (profile == null) {
            return null;                    
        }
        
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta headMeta = head.getItemMeta();
        Class<?> headMetaClass = headMeta.getClass();
        if (!Reflection.set(headMetaClass, headMeta, "profile",
                profile,
                "Unable to inject porofile")) {
            return null;
        }

        head.setItemMeta(headMeta);
        return head;
    }    

    @Override
    public boolean updateSkull(Skull skull, String url) {
        if (skull == null) {
            return false;
        }
        
        GameProfile profile = createGameProfile(url);
        
        if (profile == null) {
            return false;
        }
        
        skull.setSkullType(SkullType.PLAYER);
        
        Class<?> skullClass = skull.getClass();
        if (!Reflection.set(skullClass, skull, "profile",
                profile,
                "Unable to inject porofile")) {
            return false;
        }
        
        skull.update();
        return true;
    }
}
