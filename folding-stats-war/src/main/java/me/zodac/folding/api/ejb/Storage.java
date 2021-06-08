package me.zodac.folding.api.ejb;


/**
 * In order to decouple both the REST layer and the {@link BusinessLogic} from the persistence solution we use this
 * interface - to be implemented as an EJB - as a single point of entry for CRUD operations.
 * Since some of the persisted data can be cached, we don't want any other modules of the
 * codebase to need to worry about DB vs cache access, and instead encapsulate all of that logic here.
 */
public interface Storage {

//    public Hardware createHardware(final Hardware hardware) 
//
//    public Collection<Hardware> getAllHardware() 
//
//    public Optional<Hardware> getHardwareForUser(final User user)
//
//    public void updateHardware(final Hardware updatedHardware) throws HardwareNotFoundException, FoldingExternalServiceException
//
//    public void deleteHardware(final int hardwareId) 
//
//    public User createUser(final User user) throws FoldingExternalServiceException
//
//    public User getUser(final int userId) throws UserNotFoundException
//
//    public User getUserWithPasskey(final int userId, final boolean showFullPasskeys) throws UserNotFoundException
//
//    public Collection<User> getAllUsers() 
//
//    public Collection<User> getAllUsersWithPasskeys(final boolean showFullPasskeys) 
//
//    public void updateUser(final User updatedUser) throws UserNotFoundException, FoldingExternalServiceException
//
//    public void deleteUser(final int userId) 
//
//    public Team createTeam(final Team team) 
//
//    public Team getTeam(final int teamId) throws TeamNotFoundException
//
//    public Collection<Team> getAllTeams() 
//
//    public void updateTeam(final Team team) 
//
//    public void deleteTeam(final int teamId) 
//
//    public boolean doesNotContainHardware(final int hardwareId)
//
//    public boolean doesNotContainTeam(final int teamId)
//
//    public Collection<User> getUsersOnTeam(final Team team) 
//
//    public Optional<Hardware> getHardwareWithName(final String hardwareName)
//
//    public Optional<Team> getTeamWithName(final String teamName)
//
//    public Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey)
//
//    public Optional<User> getUserWithHardware(final Hardware hardware)
//
//    public Optional<User> getUserWithTeam(final Team team)
//
//    public void persistInitialUserStats(final User user) throws FoldingExternalServiceException
//
//    public void persistInitialUserStats(final UserStats userStats) 
//
//    public Stats getInitialStatsForUser(final int userId) 
//
//    public void persistHourlyTcStatsForUser(final UserTcStats userTcStats) 
//
//    public UserTcStats getTcStatsForUser(final int userId) throws UserNotFoundException, FoldingException, NoStatsAvailableException
//
//    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year) throws UserNotFoundException
//
//    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) throws UserNotFoundException
//
//    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) 
//
//    public void addOffsetStats(final int userId, final OffsetStats offsetStats) 
//
//    public void addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) 
//
//    public OffsetStats getOffsetStatsForUser(final int userId) 
//
//    public void initialiseOffsetStats() 
//
//    public void clearOffsetStats() 
//
//    public void persistTotalStatsForUser(final UserStats stats) 
//
//    public Stats getTotalStatsForUser(final int userId) 
//
//    public void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException
//
//    public SystemUserAuthentication authenticateSystemUser(final String userName, final String password) 
//
//    public Collection<RetiredUserTcStats> getRetiredUsersForTeam(final Team team) 
//
//    public void deleteRetiredUserStats() 
}
