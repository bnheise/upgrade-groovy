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

LIFERAY = 'liferay'
TABLE_LAYOUT = '%10s %30s %15s %30s'

// Portal Properties
portalProperties = PropsUtil.getProperties()

// System Properties
systemProperties = System.getProperties()

// Search Information
// This section is incompatible with 6.1
// TODO: Check if search engine method for 6.2 works here

// Groups and Sites
groups = GroupLocalServiceUtil.getGroups(-1, -1)
sites = groups.findAll { group -> group.site }

// Others
companiesCount = CompanyLocalServiceUtil.companiesCount
usersCount = UserLocalServiceUtil.usersCount
layoutsCount = LayoutLocalServiceUtil.layoutsCount
// No LayoutFriendlyURLLocalServiceUtil so we have to calculate the number as shown below
friendlyUrlCount =
  LayoutLocalServiceUtil.getLayouts(-1, -1).size() - LayoutLocalServiceUtil.getNullFriendlyURLLayouts().size()
totalDDMStructures = DDMStructureLocalServiceUtil.getDDMStructuresCount()
totalDDMTemplates = DDMTemplateLocalServiceUtil.getDDMTemplatesCount()
totalDDMContents = DDMContentLocalServiceUtil.getDDMContentsCount()
totalJournalArticles = JournalArticleLocalServiceUtil.getJournalArticlesCount()
totalDLFiles = DLFileEntryLocalServiceUtil.getDLFileEntriesCount()
portletPreferences = PortletPreferencesLocalServiceUtil.portletPreferences
portlets = portletPreferences.portletId.collect { portletId -> portletId - ~/_INSTANCE.*/ }.unique().sort();
liferayPortlets = portlets.findAll { portlet -> portlet.contains(LIFERAY) }
customPortlets = portlets.findAll { portlet -> !portlet.contains(LIFERAY) }
// No LayoutFriendlyURLLocalServiceUtil so we just get the layouts themselves
// Additionally, no page versioning system so no need to get unique friendly URLs
layouts = LayoutLocalServiceUtil.getLayouts(-1, -1)

// Summary
println 'Database: ' + portalProperties.getProperty('jdbc.default.driverClassName')
println '\nOS: ' + systemProperties.getProperty('os.name')
println '\n* App Server: TDB'

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
println 'Total Layouts FriendlyURLs: ' + friendlyUrlCount
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

layouts.each { layout ->
  row = [layout.companyId, layout.createDate, layout.privateLayout, layout.friendlyURL]
  println(sprintf(TABLE_LAYOUT, row))
}

println '\nTotal Pages: ' + layouts.size()
println '\n--------------------'
println '\nDate: ' + new Date()
