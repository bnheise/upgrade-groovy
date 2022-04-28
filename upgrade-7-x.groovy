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

LIFERAY = 'liferay'
TABLE_LAYOUT = '%10s %30s %15s %30s'

// Portal Properties
portalProperties = PropsUtil.getProperties(true).entrySet()
portalPropertiesMap = portalProperties.collectEntries { entry ->
    [(entry.key): entry.value]
}

// System Properties
systemProperties = System.getProperties().entrySet()
systemPropertiesMap = systemProperties.collectEntries { entry ->
    [(entry.key): entry.value]
}

// Search Information
searchEngineInformation = null
try {
    bundle = FrameworkUtil.getBundle(GroovyExecutor.class)
    st = new ServiceTracker(bundle.bundleContext, SearchEngineInformation.class, null)
    st.open()
    if (!st.isEmpty()) {
        searchEngineInformation = st.service
    }
}
catch (Exception e) {
    println e
}
finally {
    if (st != null) {
        st.close()
    }
}

// Groups and Sites
groups = GroupLocalServiceUtil.getGroups(-1, -1)
sites = groups.findAll { group -> group.site }

// Others
companies = CompanyLocalServiceUtil.companiesCount
users = UserLocalServiceUtil.usersCount
layouts = LayoutLocalServiceUtil.layoutsCount
totalLayoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.layoutFriendlyURLsCount
totalDDMStructures = DDMStructureLocalServiceUtil.getDDMStructuresCount()
totalDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplatesCount()
totalDDMContents = DDMContentLocalServiceUtil.getDDMContentsCount()
totalJournalArticles = JournalArticleLocalServiceUtil.getJournalArticlesCount()
totalDLFiles = DLFileEntryLocalServiceUtil.getDLFileEntriesCount()
portletPreferences = PortletPreferencesLocalServiceUtil.portletPreferences
portlets = portletPreferences.portletId.collect { portletId -> portletId - ~/_INSTANCE.*/ }.unique().sort()
layoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.getLayoutFriendlyURLs(0, 99999)
uniqueFriendlyURLs = layoutFriendlyURLs
        .findAll { url -> url.mvccVersion == 1 }
liferayPortlets = portlets
        .findAll { portlet -> portlet.contains(LIFERAY) }
customPortlets = portlets
        .findAll { portlet -> !portlet.contains(LIFERAY) }

// Summary

println 'Database: ' + portalPropertiesMap['jdbc.default.driverClassName']
println '\nOS: ' + systemPropertiesMap['os.name']
println '\n* App Server: TDB'
println '\nSearch: ' + searchEngineInformation.vendorString + ' - ' + searchEngineInformation.clientVersionString

println '\nSites:'
sites.each { site ->
    println '\t' + site.friendlyURL
    liveGroup = StagingUtil.getLiveGroup(site.groupId)
    println '\t\tStaging: ' + liveGroup.staged
}

println '\nPersonal Public Pages: ' + portalPropertiesMap['layout.user.public.layouts.enabled']
println 'Personal Private Pages: ' + portalPropertiesMap['layout.user.private.layouts.enabled']

println '\nTotal Companies: ' + companies
println 'Total Users: ' + users
println 'Total Layouts: ' + layouts
println 'Total Layouts FriendlyURLs: ' + totalLayoutFriendlyURLs
if (layouts > users) {
    println 'Total Users Layouts: ' + (users * 2)
    println 'Total Custom Layouts: ' + (layouts - (users * 2))
}
println 'Total DDMStructures: ' + totalDDMStructures
println 'Total DDMTemplates: ' + totalDDMTemplates
println 'Total DDMContents: ' + totalDDMContents
println 'Total JournalArticles: ' + totalJournalArticles
println 'Total DLFiles: ' + totalDLFiles

println '\nTotal Liferay Portlets: ' + liferayPortlets.size()
/* groovylint-disable-next-line DuplicateStringLiteral */
liferayPortlets.each { portlet -> println '\t' + portlet }
println 'Total Custom Portlets: ' + customPortlets.size()
/* groovylint-disable-next-line DuplicateStringLiteral */
customPortlets.each { portlet -> println '\t' + portlet }

println '\nPages:'
totalPages = 0
names = ['companyId', 'createDate', 'privateLayout', 'friendlyURL']
println(sprintf(TABLE_LAYOUT, names))

uniqueFriendlyURLs
        .each { url ->
            row = [url.companyId, url.createDate, url.privateLayout, url.friendlyURL]
            println(sprintf(TABLE_LAYOUT, row))
        }

println '\nTotal Pages: ' + uniqueFriendlyURLs.size()
println 'Total Versions: ' + layoutFriendlyURLs.size()
println '\n--------------------'
println '\nDate: ' + new Date()
