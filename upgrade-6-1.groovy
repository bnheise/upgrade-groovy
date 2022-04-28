import com.liferay.portal.util.PropsUtil;
import org.osgi.framework.FrameworkUtil;
import com.liferay.portal.scripting.groovy.GroovyExecutor;
import org.osgi.util.tracker.ServiceTracker;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.service.DDMContentLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.staging.StagingUtil;

// Portal Properties
portalProperties = PropsUtil.getProperties();

// System Properties
systemProperties = System.getProperties();

// Search Information
// This section is incompatible with 6.1
// Question: Is there some other way to get info about the search engine?
// def searchEngineInformation = null
// try {
//     bundle = FrameworkUtil.getBundle(GroovyExecutor.class)
//     st = new ServiceTracker(bundle.bundleContext, SearchEngineInformation.class, null)
//     st.open();
//     if (!st.isEmpty()) {
//         searchEngineInformation = st.service
//     }
// }
// catch(Exception e) {
//     println e
// }
// finally {
//     if (st != null) {
//         st.close();
//     }
// }

// Groups and Sites
groups = GroupLocalServiceUtil.getGroups(-1, -1)
sites = groups.findAll { it.site }

// Others
def companiesCount = CompanyLocalServiceUtil.companiesCount;
def usersCount = UserLocalServiceUtil.usersCount;
def layoutsCount = LayoutLocalServiceUtil.layoutsCount;
// No LayoutFriendlyURLLocalServiceUtil so we have to calculate the number as shown below
def friendlyUrlCount = LayoutLocalServiceUtil.getLayouts(-1, -1).size() - LayoutLocalServiceUtil.getNullFriendlyURLLayouts().size();
def totalDDMStructures = DDMStructureLocalServiceUtil.getDDMStructuresCount();
def totalDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplatesCount();
def totalDDMContents = DDMContentLocalServiceUtil.getDDMContentsCount();
def totalJournalArticles = JournalArticleLocalServiceUtil.getJournalArticlesCount();
def totalDLFiles = DLFileEntryLocalServiceUtil.getDLFileEntriesCount();
def portletPreferences = PortletPreferencesLocalServiceUtil.portletPreferences;
def portlets = portletPreferences.portletId.collect{it - ~/_INSTANCE.*/}.unique().sort();
def liferayPortlets = portlets.findAll{it.contains('liferay')}
def customPortlets = portlets.findAll{!it.contains('liferay')}
// No LayoutFriendlyURLLocalServiceUtil so we just get the layouts themselves
// Additionally, no page versioning system so no need to get unique friendly URLs
def layouts = LayoutLocalServiceUtil.getLayouts(-1, -1);

// Summary
println "Database: " + portalProperties.getProperty("jdbc.default.driverClassName");
println "\nOS: " + systemProperties.getProperty("os.name");
println "\n* App Server: TDB"

println "\nSites:"
sites.each{ site ->
    println "\t" + site.friendlyURL
    liveGroup = StagingUtil.getLiveGroup(site.groupId);
    println "\t\tStaging: " + liveGroup.staged
}

println "\nPersonal Public Pages: " + portalProperties.getProperty("layout.user.public.layouts.enabled")
println "Personal Private Pages: " + portalProperties.getProperty("layout.user.private.layouts.enabled")

println '\nTotal Companies: ' + companiesCount
println 'Total Users: ' + usersCount
println 'Total Layouts: ' + layoutsCount
println 'Total Layouts FriendlyURLs: ' + friendlyUrlCount
if (layoutsCount > usersCount){
    println 'Total Users Layouts: ' + (users*2)
    println 'Total Custom Layouts: ' + (layouts-(users*2))
}
println 'Total DDMStructures: '+totalDDMStructures
println 'Total DDMTemplates: '+totalDDMTemplates
println 'Total DDMContents: '+totalDDMContents
println 'Total JournalArticles: '+totalJournalArticles
println 'Total DLFiles: '+totalDLFiles

println '\nTotal Liferay Portlets: ' + liferayPortlets.size()
liferayPortlets.each{
  println "\t" + it
}
println 'Total Custom Portlets: ' + customPortlets.size()
customPortlets.each{
  println "\t" + it
}

println '\nPages:'
def totalPages = 0;
def names = ['companyId', 'createDate', 'privateLayout', 'friendlyURL']
println( sprintf('%10s %30s %15s %30s', names) )

layouts.each{
  def row = [it.companyId, it.createDate, it.privateLayout, it.friendlyURL]
  println( sprintf('%10s %30s %15s %30s', row) )
}

println '\nTotal Pages: ' + layouts.size()
// Removed Total Versions because there are no page versions
// println 'Total Versions: ' + layoutFriendlyURLs.size()
println '\n--------------------'
println '\nDate: ' + new Date()