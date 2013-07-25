package net.bogdoll.netbeans.tools.lfr;

import java.io.File;
import java.util.logging.Logger;
import static net.bogdoll.netbeans.tools.lfr.Bundle.*;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CookieAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@ActionID(category = "Tools", id = "net.bogdoll.netbeans.tools.lfr.LockFileRemover")
@ActionRegistration(displayName = "#CTL_LockFileRemover", lazy = false, asynchronous = true)
@ActionReferences({
    @ActionReference(path="Toolbars/Build", position = 250),
    @ActionReference(path="Projects/Actions")
})
@Messages("CTL_LockFileRemover=Remove Lock File")
public final class LockFileRemover extends CookieAction
{
    private final static Logger LOG = Logger.getLogger(LockFileRemover.class.getName());

    public LockFileRemover() {
        putValue("hideWhenDisabled", "true");
    }
    
    @Override
    protected String iconResource() {
        return "net/bogdoll/netbeans/tools/lfr/draw-eraser.png"; // NOI18N
    }
    
    private boolean isRelevant(SuiteProject context) {
        File testUserDirLockFile = context.getTestUserDirLockFile();
        return testUserDirLockFile.exists() && testUserDirLockFile.isFile();
    }

    @Override
    protected int mode() {
        return MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{SuiteProject.class};
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            SuiteProject project = n.getLookup().lookup(SuiteProject.class);
            if(project!=null && isRelevant(project)) {
                return true;
            }
        }
        return false;
    }
    
    private void deleteLockFile(SuiteProject context) {
        File testUserDirLockFile = context.getTestUserDirLockFile();
        if(!testUserDirLockFile.delete()) {
            InputOutput io = IOProvider.getDefault().getIO("Test user dir lock file remover", false);
            io.getErr().println("Can't delete test user dir lock file: "+testUserDirLockFile.getAbsolutePath());
            io.getErr().close();
        }
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node n : activatedNodes) {
            SuiteProject project = n.getLookup().lookup(SuiteProject.class);
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