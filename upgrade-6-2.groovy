import com.liferay.portal.util.PropsUtil
import com.liferay.portal.service.GroupLocalServiceUtil
import com.liferay.portal.service.CompanyLocalServiceUtil
import com.liferay.portal.service.UserLocalServiceUtil
import com.liferay.portal.service.LayoutLocalServiceUtil
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil
import com.liferay.portlet.dynamicdatamapping.service.DDMTemplateLocalServiceUtil
import com.liferay.portlet.dynamicdatamapping.service.DDMContentLocalServiceUtil
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil
import com.liferay.portal.kernel.staging.StagingUtil
import com.liferay.portal.kernel.search.SearchEngineUtil
import com.liferay.portal.service.LayoutFriendlyURLLocalServiceUtil
import com.liferay.portal.service.PortletLocalServiceUtil

LIFERAY = 'liferay'
TABLE_LAYOUT = '%10s %30s %15s %30s'

// Portal Properties
portalProperties = PropsUtil.getProperties()

// System Properties
systemProperties = System.getProperties()

// Search Information
// Note: Can't get client version ID
searchEngineIds = SearchEngineUtil.getSearchEngineIds()

// Groups and Sites
groups = GroupLocalServiceUtil.getGroups(-1, -1)
sites = groups.findAll { group -> group.site }

// Others
companiesCount = CompanyLocalServiceUtil.companiesCount
usersCount = UserLocalServiceUtil.usersCount
layoutsCount = LayoutLocalServiceUtil.layoutsCount
totalLayoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.layoutFriendlyURLsCount
totalDDMStructures = DDMStructureLocalServiceUtil.getDDMStructuresCount()
totalDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplatesCount()
totalDDMContents = DDMContentLocalServiceUtil.getDDMContentsCount()
totalJournalArticles = JournalArticleLocalServiceUtil.getJournalArticlesCount()
totalDLFiles = DLFileEntryLocalServiceUtil.getDLFileEntriesCount()
portletPreferences = PortletPreferencesLocalServiceUtil.portletPreferences

portlets = portletPreferences.portletId.collect { portletId ->
  idNum = portletId.split('_')[0]
  portlet = PortletLocalServiceUtil.getPortletById(idNum)
  if (portlet != null) {
    portlet.getPluginPackage().getPackageId() + ' - ' + portlet.getDisplayName()
  } else {
    return portletId
  }
 }.unique().sort();

layoutFriendlyURLs = LayoutFriendlyURLLocalServiceUtil.getLayoutFriendlyURLs(-1, -1)
// There is no mvccVersion on a Friendly URL in 6.2
// def uniqueFriendlyURLs = layoutFriendlyURLs.findAll{it.mvccVersion == 1}
liferayPortlets = portlets.findAll { portlet -> portlet.contains(LIFERAY) }
customPortlets = portlets.findAll { portlet -> !portlet.contains(LIFERAY) }

// Summary
println 'Database: ' + portalProperties.getProperty('jdbc.default.driverClassName')
println '\nOS: ' + systemProperties.getProperty('os.name')
println '\n* App Server: TDB'
searchEngineIds.each { engineId ->
  println '\n Search Engine Id: ' + engineId
  println 'Vendor: ' + SearchEngineUtil.getSearchEngine(engineId).getVendor()
}

println '\nSites:'
sites.each { site ->
    println '\t' + site.friendlyURL
    liveGroup = StagingUtil.getLiveGroup(site.groupId)
    println '\t\tStaging: ' + liveGroup.staged
}

println '\nPersonal Public Pages: ' + portalProperties.getProperty('layout.user.public.layouts.enabled')
println 'Personal Private Pages: ' + portalProperties.getProperty('layout.user.private.layouts.enabled')

println '\nTotal Companies: ' + companiesCount
println 'Total Users: ' + usersCount
println 'Total Layouts: ' + layoutsCount
println 'Total Layouts FriendlyURLs: ' + totalLayoutFriendlyURLs
if (layoutsCount > usersCount) {
  println 'Total Users Layouts: ' + (users * 2)
  println 'Total Custom Layouts: ' + (layouts - (users * 2))
}
println 'Total DDMStructures: ' + totalDDMStructures
println 'Total DDMTemplates: ' + totalDDMTemplates
println 'Total DDMContents: ' + totalDDMContents
println 'Total JournalArticles: ' + totalJournalArticles
println 'Total DLFiles: ' + totalDLFiles

println '\nTotal Liferay Portlets: ' + liferayPortlets.size()
liferayPortlets.each { portlet ->
  /* groovylint-disable-next-line DuplicateStringLiteral */
  println '\t' + portlet
}
println 'Total Custom Portlets: ' + customPortlets.size()
customPortlets.each { portlet ->
  /* groovylint-disable-next-line DuplicateStringLiteral */
  println '\t' + portlet
}

println '\nPages:'
totalPages = 0
names = ['companyId', 'createDate', 'privateLayout', 'friendlyURL']
println(sprintf(TABLE_LAYOUT, names))

layoutFriendlyURLs.each { layout ->
  row = [layout.companyId, layout.createDate, layout.privateLayout, layout.friendlyURL]
  println(sprintf(TABLE_LAYOUT, row))
}

println 'Total Pages: ' + layoutFriendlyURLs.size()
println '\n--------------------'
println '\nDate: ' + new Date()
