import com.liferay.portal.util.PropsUtil
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil
import com.liferay.portal.kernel.service.UserLocalServiceUtil
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalServiceUtil
import com.liferay.dynamic.data.mapping.service.DDMContentLocalServiceUtil
import com.liferay.journal.service.JournalArticleLocalServiceUtil
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil
import com.liferay.portal.kernel.service.LayoutFriendlyURLLocalServiceUtil
import com.liferay.portal.kernel.service.GroupLocalServiceUtil
import com.liferay.exportimport.kernel.staging.StagingUtil
import org.osgi.util.tracker.ServiceTracker
import org.osgi.framework.FrameworkUtil
import com.liferay.portal.search.engine.SearchEngineInformation
import com.liferay.portal.scripting.groovy.internal.GroovyExecutor
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil


// Portal Properties
portalProperties = PropsUtil.getProperties(true).entrySet();
def portalPropertiesMap = portalProperties.collectEntries {
    [(it.key): it.value]
}

// System Properties
systemProperties = System.getProperties().entrySet();
def systemPropertiesMap = systemProperties.collectEntries {
    [(it.key): it.value]
}

// Search Information
def searchEngineInformation = null
try {
    bundle = FrameworkUtil.getBundle(GroovyExecutor.class)
    st = new ServiceTracker(bundle.bundleContext, SearchEngineInformation.class, null)
    st.open();
    if (!st.isEmpty()) {
        searchEngineInformation = st.service
    }
}
catch(Exception e) {
    println e
}
finally {
    if (st != null) {
        st.close();
    }
}

// Groups and Sites
groups = GroupLocalServiceUtil.getGroups(-1, -1)
sites = groups.findAll { it.site }

// Others
def companies = CompanyLocalServiceUtil.companiesCount
def users = UserLocalServiceUtil.usersCount
def layouts = LayoutLocalServiceUtil.layoutsCount
def totalLayoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.layoutFriendlyURLsCount
def totalDDMStructures = DDMStructureLocalServiceUtil.getDDMStructuresCount()
def totalDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplatesCount()
def totalDDMContents = DDMContentLocalServiceUtil.getDDMContentsCount()
def totalJournalArticles = JournalArticleLocalServiceUtil.getJournalArticlesCount()
def totalDLFiles = DLFileEntryLocalServiceUtil.getDLFileEntriesCount()
def portletPreferences = PortletPreferencesLocalServiceUtil.portletPreferences
def portlets = portletPreferences.portletId.collect{it - ~/_INSTANCE.*/}.unique().sort()
def layoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.getLayoutFriendlyURLs(0,99999)
def uniqueFriendlyURLs = layoutFriendlyURLs
        .findAll{it.mvccVersion == 1}
def liferayPortlets = portlets
        .findAll{it.contains('liferay')}
def customPortlets = portlets
        .findAll{!it.contains('liferay')}

// Summary

println "Database: " + portalPropertiesMap["jdbc.default.driverClassName"]
println "\nOS: " + systemPropertiesMap["os.name"]
println "\n* App Server: TDB"
println "\nSearch: " + searchEngineInformation.vendorString + " - " + searchEngineInformation.clientVersionString

println "\nSites:"
sites.each{ site ->
    println "\t" + site.friendlyURL
    liveGroup = StagingUtil.getLiveGroup(site.groupId);
    println "\t\tStaging: " + liveGroup.staged
}

println "\nPersonal Public Pages: " + portalPropertiesMap["layout.user.public.layouts.enabled"]
println "Personal Private Pages: " + portalPropertiesMap["layout.user.private.layouts.enabled"]


println '\nTotal Companies: '+companies
println 'Total Users: '+users
println 'Total Layouts: '+layouts
println 'Total Layouts FriendlyURLs: '+totalLayoutFriendlyURLs
if (layouts > users){
    println 'Total Users Layouts: '+(users*2)
    println 'Total Custom Layouts: '+(layouts-(users*2))
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

uniqueFriendlyURLs
        .each{
            def row = [it.companyId, it.createDate, it.privateLayout, it.friendlyURL]
            println( sprintf('%10s %30s %15s %30s', row) )
        }

println '\nTotal Pages: ' + uniqueFriendlyURLs.size()
println 'Total Versions: ' + layoutFriendlyURLs.size()
println '\n--------------------'
println '\nDate: ' + new Date()