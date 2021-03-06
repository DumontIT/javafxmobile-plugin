/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2018, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javafxports.jfxmobile.plugin;

import com.gluonhq.gvmbuild.BosonAppBuilder;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.javafxports.jfxmobile.plugin.ios.IosExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gvm {

    public static void build(String target, Project project ) {
        GvmConfig config = new GvmConfig(project);
        boolean isLaunchOnDevice = "device".equals(target);

        SourceSetContainer sourceSetContainer = (SourceSetContainer) project.getProperties().get("sourceSets");
        SourceSet mainSourceSet = sourceSetContainer.findByName("main");

        List<File> classes = new ArrayList<>();
        List<File> resources = new ArrayList<>();
        if (mainSourceSet != null) {
            classes.addAll(mainSourceSet.getOutput().getClassesDirs().getFiles());
            resources.add(mainSourceSet.getOutput().getResourcesDir());
        }
        SourceSet iosSourceSet = sourceSetContainer.findByName("ios");
        if (iosSourceSet != null) {
            classes.addAll(iosSourceSet.getOutput().getClassesDirs().getFiles());
            resources.add(iosSourceSet.getOutput().getResourcesDir());
        }

        try {
            String vm = "boson"; 
            BosonAppBuilder appBuilder = new BosonAppBuilder();
            appBuilder.vm(vm)
                    .rootDir(config.getRootDirName())
                    .classesDirs(classes)
                    .resourcesDirs(resources)
                    .appId(config.getMainClassName())
                    .appName(config.getAppName())
                    .forcelinkClasses(Arrays.asList(config.getForcelinkClasses()))
                    .jarDependencies( config.getJarDependecies());
            String tempDir =  project.getExtensions().findByType(JFXMobileExtension.class).getIosExtension().getTemporaryDirectory().getAbsolutePath();
            String nativeLibDir =  project.getExtensions().findByType(JFXMobileExtension.class).getIosExtension().getNativeDirectory();

            List<String> nativeLibs = new ArrayList<>();

            File nativeDir = new File(nativeLibDir);
            if (nativeDir.exists() && nativeDir.isDirectory()) {
                for (File nativeLib:   nativeDir.listFiles()) {
                    nativeLibs.add(nativeLib.getAbsolutePath());
                }
            }
            File nativeTmpDir = new File(tempDir, "native");
            if (nativeTmpDir.exists()) {
                for (File nativeLib:   nativeTmpDir.listFiles()) {
                    nativeLibs.add(nativeLib.getAbsolutePath());
                }
            }
            appBuilder.nativeLibs(nativeLibs);

            if (isLaunchOnDevice) {
                appBuilder.arch("arm64");
            }
            appBuilder.build();
            if (isLaunchOnDevice) {
                appBuilder.launchOnDevice(config.getLaunchDir());
            } else {
                appBuilder.launchOnSimulator(config.getLaunchDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
