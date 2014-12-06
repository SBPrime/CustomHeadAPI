/*
 * CustomHeadAPI is a library plugin for Minecraft that allows simple cration of
 * player heads that use custom textures.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) CustomHeadAPI contributors
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
package org.primesoft.customheadapi;

import org.primesoft.customheadapi.implementation.FallbackHeadCreator;
import org.primesoft.customheadapi.implementation.CustomHeadCreator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author SBPrime
 */
public class CustomHeadApi extends JavaPlugin {

    private static final Logger s_log = Logger.getLogger("Minecraft.CustomHeadApi");
    private static ConsoleCommandSender s_console;
    private static String s_prefix = null;
    private static final String s_logFormat = "%s %s";
    private static CustomHeadApi s_instance;

    /**
     * The head creator class
     */
    private IHeadCreator m_headCreator;

    /**
     * The api instance
     *
     * @return
     */
    public static CustomHeadApi getInstance() {
        return s_instance;
    }

    static String getPrefix() {
        return s_prefix;
    }

    public IHeadCreator getHeadCreator() {
        return m_headCreator;
    }

    /**
     * Send message to the log
     *
     * @param msg
     */
    public static void log(String msg) {
        if (s_log == null || msg == null || s_prefix == null) {
            return;
        }

        s_log.log(Level.INFO, String.format(s_logFormat, s_prefix, msg));
    }

    @Override
    public void onEnable() {
        s_instance = this;
        PluginDescriptionFile desc = getDescription();
        s_prefix = String.format("[%s]", desc.getName());
        s_console = getServer().getConsoleSender();

        try {
            m_headCreator = new CustomHeadCreator();
            ItemStack headItem = m_headCreator.createItemStack("foo.org");
            if (headItem == null) {
                log("Something went wrong, using the fallback head creator. No custom heads available :(");
                log("Send the above message to the author of the plugin.");
                m_headCreator = new FallbackHeadCreator();
            }
        } catch (Error er) {
            log("Something went wrong, using the fallback head creator. No custom heads available :(");
            log("----------------------------------------------------------------");
            log("Message: " + er.getMessage());
            log("Stack: ");
            for (StackTraceElement element : er.getStackTrace()) {
                log(" " + element.toString());
            }
            log("Send the above message to the author of the plugin.");
            log("----------------------------------------------------------------");
            m_headCreator = new FallbackHeadCreator();
        }

        log("Enabled");
    }

    @Override
    public void onDisable() {
        log("Disabled");
    }
}
