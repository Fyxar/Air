package com.dev.air.module.api.manager;

import com.dev.air.Client;
import com.dev.air.font.impl.CustomFontRenderer;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.Development;
import com.dev.air.module.impl.combat.*;
import com.dev.air.module.impl.exploit.*;
import com.dev.air.module.impl.movement.*;
import com.dev.air.module.impl.test.FontTestMod;
import com.dev.air.module.impl.test.TestMod;
import com.dev.air.module.impl.render.*;
import com.dev.air.module.impl.world.ScaffoldMod;

import java.util.ArrayList;

import static com.dev.air.util.MinecraftInstance.*;

public class ModuleManager extends ArrayList<Module> {

    public ModuleManager() {
        /* Combat */
        this.add(new KillAuraMod());
        this.add(new VelocityMod());
        //this.add(new TickBaseMod());
        this.add(new CriticalsMod());
        this.add(new BackTrackMod());

        /* Movement */
        this.add(new NoSlowMod());
        this.add(new SpeedMod());
        this.add(new JesusMod());
        this.add(new FlightMod());
        this.add(new SprintMod());

        /* Render */
        this.add(new ArraylistMod());
        this.add(new ClickUIMod());
        this.add(new AnimationsMod());
        this.add(new WatermarkMod());
        this.add(new TargetHUDMod());
        this.add(new BPSMod());

        /* World */
        this.add(new ScaffoldMod());

        /* Exploit */
        this.add(new DisablerMod());
        this.add(new FastUseMod());
        this.add(new RegenMod());

        /* Testing */
        this.add(new TestMod());
        this.add(new FontTestMod());
    }

    @Override
    public boolean add(Module module) {
        if (module.getClass().getDeclaredAnnotation(Development.class) != null && !Client.isDevelopment) return false;

        return super.add(module);
    }

    public Module get(Class klass) {
        for (Module module : this) if (module.getClass() == klass) return module;

        return null;
    }

    public Module get(String name) {
        for (Module module : this) if (module.getName().equalsIgnoreCase(name)) return module;

        return null;
    }

    public ArrayList<Module> get(Category category) {
        ArrayList<Module> mods = new ArrayList<>();
        for (Module module : this) if (module.getCategory() == category) mods.add(module);

        return mods;
    }

    public ArrayList<Module> sort(CustomFontRenderer renderer) {
        ArrayList<Module> mods = new ArrayList<>();

        for (Module module : this) mods.add(module);

        if (renderer == null) {
            mods.sort((m2, m1) -> (int) (mc.fontRenderer.getStringWidth(m1.getPrefix().isEmpty() ?
                    m1.getName() : m1.getName() + " §7" + m1.getPrefix()) - mc.fontRenderer.getStringWidth(
                    m2.getPrefix().isEmpty() ?
                            m2.getName() : m2.getName() + " §7" + m2.getPrefix()
            )));
        } else {
            mods.sort((m2, m1) -> (int) (renderer.getStringWidth(m1.getPrefix().isEmpty() ?
                    m1.getName() : m1.getName() + " §7" + m1.getPrefix()) - renderer.getStringWidth(
                    m2.getPrefix().isEmpty() ?
                            m2.getName() : m2.getName() + " §7" + m2.getPrefix()
            )));
        }

        return mods;
    }

}
