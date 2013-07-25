package net.bogdoll.netbeans.tools.svi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CookieAction;
import static net.bogdoll.netbeans.tools.svi.Bundle.*;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.openide.filesystems.FileLock;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@ActionID(category = "Tools", id = "net.bogdoll.netbeans.tools.svi.SpecVerInc")
@ActionRegistration(displayName = "#CTL_SpecVerInc", lazy = false, asynchronous = true )
@ActionReferences({
    @ActionReference(path="Toolbars/Build", position = 250),
    @ActionReference(path="Projects/Actions")
})
@Messages("CTL_SpecVerInc=Specification Version Incrementer")
public final class SpecVerInc extends CookieAction
{
    private final static Logger LOG = Logger.getLogger(SpecVerInc.class.getName());
    private final static String KEY = "OpenIDE-Module-Specification-Version";

    public SpecVerInc() {
        putValue("hideWhenDisabled", "true");
    }

    @Override
    protected int mode() {
        return MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{NbModuleProject.class};
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            NbModuleProject project = n.getLookup().lookup(NbModuleProject.class);
            if(project!=null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            NbModuleProject project = n.getLookup().lookup(NbModuleProject.class);
            incrementSpecVersion(project);
        }
    }

    @Override
    public String getName() {
        return CTL_SpecVerInc();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SpecVerInc.class);
    }

    private void incrementSpecVersion(NbModuleProject project) {
        IOProvider ioProvider = IOProvider.getDefault();
        InputOutput io = ioProvider.getIO("Hello", false);
        io.select();
        try {
            Manifest manifest = project.getManifest();
            if(manifest!=null) {
                Attributes attr = manifest.getMainAttributes();
                String specVersion = attr.getValue( KEY );
                String newSpecVersion = incVersion(specVersion);
                attr.putValue(KEY, newSpecVersion);

                FileObject manifestFile = project.getManifestFile();
                if(manifestFile!=null) {
                    try {
                        FileLock lock = manifestFile.lock();
                        try {
                            OutputStream out = manifestFile.getOutputStream(lock);
                            if(out!=null) {
                                manifest.write(out);
                                out.close();
                                String projectName = computeProjectName(project);
                                io.getOut().println("Increased spec version of project "+projectName+" from "+specVersion+" to "+newSpecVersion);
                            }
                            else {
                                io.getErr().println("Can't write the updated manifest");
                            }
                        }
                        finally {
                            lock.releaseLock();
                        }
                    }
                    catch(IOException e) {
                        io.getErr().println("Can't update the manifest: "+e.getLocalizedMessage());
                    }
                }
                else {
                    io.getErr().println("Can't find the manifest file");
                }
            }
            else {
                io.getErr().println("Can't find any manifest");
            }
        }
        finally {
            io.getErr().close();
        }
    }

    private String incVersion(String value) {
        int lastIndexOf = value.lastIndexOf('.')+1;
        if(lastIndexOf<=0) {
            long l = Long.parseLong(value);
            return Long.toString(l+1);
        }
        else {
            String first = value.substring(0, lastIndexOf);
            long l = Long.parseLong(value.substring(lastIndexOf));
            return first+Long.toString(l+1);
        }
    }

    private String computeProjectName(NbModuleProject nbModule) {
        try {
            FileObject suiteDir = getSuiteDir(nbModule.getProjectDirectory());
            Properties suiteProperties = getSuiteProperties(suiteDir);
            return suiteProperties.getProperty("project."+nbModule.getCodeNameBase());
        }
        catch(IOException e) {
            return nbModule.getCodeNameBase();
        }
    }

    private FileObject getSuiteDir(FileObject projectDirectory) throws IOException {
        FileObject suiteLocator = projectDirectory.getFileObject("nbproject/suite.properties");
        Properties props = new Properties();
        InputStream in = suiteLocator.getInputStream();
        try {
            props.load(in);
        }
        finally {
            in.close();
        }
        String relSuitePath = props.getProperty("suite.dir").replaceAll("\\$\\{basedir\\}", "");
        return projectDirectory.getFileObject(relSuitePath);
    }

    private Properties getSuiteProperties(FileObject suiteDir) throws IOException {
        Properties props = new Properties();
        FileObject projectProperties = suiteDir.getFileObject("nbproject/project.properties");
        InputStream in = projectProperties.getInputStream();
        try {
            props.load(in);
        }
        finally {
            in.close();
        }
        return props;
    }
}