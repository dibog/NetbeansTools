package net.bogdoll.netbeans.tools.lfr;

import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.Action;
import static net.bogdoll.netbeans.tools.lfr.Bundle.*;
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

@ActionID(category = "Tools",
    id = "net.bogdoll.netbeans.tools.lfr.LockFileRemover")
@ActionRegistration(iconBase = "net/bogdoll/netbeans/tools/lfr/draw-eraser.png",
    displayName = "#CTL_LockFileRemover")
@ActionReferences({
    @ActionReference(path="Toolbars/Build", position = 250),
    @ActionReference(path="Projects/Actions")
})
@Messages("CTL_LockFileRemover=Remove Lock File")
public final class LockFileRemover extends CookieAction
{
    private final static Logger LOG = Logger.getLogger(LockFileRemover.class.getName());

    public LockFileRemover() {
    }
    
    public LockFileRemover(Project aContext) {
        putValue("hideWhenDisabled", "true");
    }

    @Override
    protected String iconResource() {
        return "net/bogdoll/netbeans/tools/lfr/draw-eraser.png"; // NOI18N
    }
        
    @Override
    public Action createContextAwareInstance(Lookup aLookup) {
        return new LockFileRemover(aLookup.lookup(Project.class));
    }
    
    private boolean isRelevant(Project context) {
        FileObject lockFile = getLockFile(context);
        return lockFile!=null && lockFile.isData();
    }

    @Override
    protected int mode() {
        return MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{Project.class};
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            Project project = n.getLookup().lookup(Project.class);
            if(isRelevant(project)) {
                return true;
            }
        }
        return false;
    }
    
    private FileObject getLockFile(Project context) {
        LOG.info("Project: "+context);
        if(context==null) return null;
        
        FileObject projectDir = context.getProjectDirectory();
        LOG.info("projectDir: "+projectDir);
        if(projectDir==null) return null;
        
        FileObject build = projectDir.getFileObject("build");
        LOG.info("build: "+build);
        if(build==null) return null;
        
        FileObject testDir = build.getFileObject("testuserdir");
        LOG.info("testDir: "+testDir);
        if(testDir==null) return null;

        return testDir.getFileObject("lock");
    }

    private void deleteLockFile(Project context) {
        try {
            FileObject lock = getLockFile(context);
            lock.delete();
        }
        catch(IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            Project project = n.getLookup().lookup(Project.class);
            if(isRelevant(project)) {
                deleteLockFile(project);
            }
        }
    }

    @Override
    public String getName() {
        return CTL_LockFileRemover();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(LockFileRemover.class);
    }
}